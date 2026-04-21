import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  Check, ChevronDown, Share, FileText, Calendar, Clock, Folder, Tag,
  X, Bold, Italic, Underline, Heading1, Heading2, List, 
  ListOrdered, CheckSquare, Link, Image as ImageIcon, Sparkles, Plus, Star, Trash2 
} from 'lucide-react';
import ExportModal from '../components/shared/ExportModal';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import ConfirmModal from '../components/ConfirmModal';
import './Editor.css';

const Editor = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { workspaceId, setWorkspaceId, workspaces, user } = useAuth();

  const [isExportOpen, setIsExportOpen] = useState(false);
  const [isFavorite, setIsFavorite] = useState(false);
  const [saveStatus, setSaveStatus] = useState('saved'); // 'saved', 'saving', 'error'
  const [isTagModalOpen, setIsTagModalOpen] = useState(false);
  const [newTagName, setNewTagName] = useState('');
  const [isAddingTag, setIsAddingTag] = useState(false);

  // Modals for Links and Images
  const [isLinkModalOpen, setIsLinkModalOpen] = useState(false);
  const [linkUrl, setLinkUrl] = useState('');
  const [isImageModalOpen, setIsImageModalOpen] = useState(false);
  const [imageUrl, setImageUrl] = useState('');
  const [confirmAction, setConfirmAction] = useState(null);
  const [isWsDropdownOpen, setIsWsDropdownOpen] = useState(false);
  const [isSectionDropdownOpen, setIsSectionDropdownOpen] = useState(false);
  const wsDropdownRef = useRef(null);
  const sectionDropdownRef = useRef(null);
  
  const [note, setNote] = useState({ title: '', content: '' });
  const [tags, setTags] = useState([]);
  const [availableTags, setAvailableTags] = useState([]);
  const [relatedNotes, setRelatedNotes] = useState([]);
  
  const editorRef = useRef(null);
  const titleRef = useRef(null);
  const saveTimeoutRef = useRef(null);
  const noteIdRef = useRef(id);

  // Close dropdowns on outside click
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (wsDropdownRef.current && !wsDropdownRef.current.contains(e.target)) setIsWsDropdownOpen(false);
      if (sectionDropdownRef.current && !sectionDropdownRef.current.contains(e.target)) setIsSectionDropdownOpen(false);
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Load note if ID is present
  useEffect(() => {
    noteIdRef.current = id;
    if (id) {
      const loadNote = async () => {
        try {
          const res = await api.get(`/notes/${id}`);
          setNote(res.data);
          setIsFavorite(res.data.favorite);
          setTags(res.data.tags || []);
          
          if (titleRef.current && titleRef.current.innerText !== res.data.title) {
            titleRef.current.innerText = res.data.title || '';
          }
          if (editorRef.current && editorRef.current.innerHTML !== res.data.content) {
            editorRef.current.innerHTML = res.data.content || '';
          }
        } catch (error) {
          console.error("Failed to load note:", error);
        }
      };
      loadNote();
    } else {
      // Clear for new note
      if (titleRef.current) titleRef.current.innerText = 'Untitled Note';
      if (editorRef.current) editorRef.current.innerHTML = '';
      setNote({ title: 'Untitled Note', content: '' });
      setIsFavorite(false);
      setTags([]);
    }
  }, [id]);

  useEffect(() => {
    if (workspaceId) {
      api.get(`/tags/workspace/${workspaceId}`).then(res => setAvailableTags(res.data)).catch(console.error);
    }
  }, [workspaceId]);

  useEffect(() => {
    if (tags.length > 0) {
      // Fetch related notes by the first tag for simplicity, or all tags
      api.get(`/notes/tag/${tags[0].id}`).then(res => {
        // Exclude current note
        setRelatedNotes(res.data.filter(n => n.id !== noteIdRef.current));
      }).catch(console.error);
    } else {
      setRelatedNotes([]);
    }
  }, [tags]);

  const handleAddTagClick = () => {
    if (!noteIdRef.current) {
      alert("Please type something to auto-save the note first.");
      return;
    }
    setIsTagModalOpen(true);
  };

  const submitTag = async () => {
    if (!newTagName.trim()) return;
    setIsAddingTag(true);
    
    let tagToUse = availableTags.find(t => t.name.toLowerCase() === newTagName.trim().toLowerCase());
    if (!tagToUse) {
      try {
        const res = await api.post('/tags', {
          name: newTagName.trim(),
          color: ['blue', 'purple', 'green', 'orange', 'red'][Math.floor(Math.random() * 5)],
          workspace: { id: workspaceId }
        });
        tagToUse = res.data;
        setAvailableTags([...availableTags, tagToUse]);
      } catch (e) {
        console.error("Failed to create tag", e);
        setIsAddingTag(false);
        return;
      }
    }
    
    if (tags.some(t => t.id === tagToUse.id)) {
      setIsAddingTag(false);
      setIsTagModalOpen(false);
      setNewTagName('');
      return; // Already added
    }

    try {
      await api.post(`/notes/${noteIdRef.current}/tags/${tagToUse.id}`);
      setTags([...tags, tagToUse]);
      setIsTagModalOpen(false);
      setNewTagName('');
    } catch (e) {
      console.error("Failed to add tag", e);
    } finally {
      setIsAddingTag(false);
    }
  };

  const handleRemoveTag = async (tagId) => {
    try {
      await api.delete(`/notes/${noteIdRef.current}/tags/${tagId}`);
      setTags(tags.filter(t => t.id !== tagId));
    } catch (e) {
      console.error("Failed to remove tag", e);
    }
  };

  const saveNote = useCallback(async (title, content) => {
    if (!workspaceId || !user) return;
    setSaveStatus('saving');

    try {
      const payload = {
        title: title || 'Untitled Note',
        content: content,
        workspace: { id: workspaceId },
        owner: { id: user.id }
      };

      if (noteIdRef.current) {
        await api.put(`/notes/${noteIdRef.current}`, payload);
        setSaveStatus('saved');
      } else {
        const res = await api.post('/notes', payload);
        noteIdRef.current = res.data.id;
        setSaveStatus('saved');
        navigate(`/editor/${res.data.id}`, { replace: true });
      }
    } catch (error) {
      console.error("Failed to save note", error);
      setSaveStatus('error');
    }
  }, [workspaceId, user, navigate]);

  const handleInput = () => {
    const currentTitle = titleRef.current?.innerText || '';
    const currentContent = editorRef.current?.innerHTML || '';

    if (saveTimeoutRef.current) {
      clearTimeout(saveTimeoutRef.current);
    }
    
    setSaveStatus('saving');
    saveTimeoutRef.current = setTimeout(() => {
      saveNote(currentTitle, currentContent);
    }, 1500); // Auto-save after 1.5s of typing inactivity
  };

  const handleFavoriteToggle = async () => {
    if (!noteIdRef.current) return; // Must be saved first
    try {
      await api.put(`/notes/${noteIdRef.current}/favorite`);
      setIsFavorite(!isFavorite);
    } catch (error) {
      console.error("Failed to toggle favorite", error);
    }
  };

  const formatText = (command, value = null) => {
    document.execCommand(command, false, value);
    if (editorRef.current) {
      editorRef.current.focus();
    }
    handleInput();
  };

  const handleLink = () => {
    setIsLinkModalOpen(true);
  };

  const submitLink = () => {
    if (linkUrl) {
      formatText('createLink', linkUrl);
    }
    setIsLinkModalOpen(false);
    setLinkUrl('');
  };

  const handleImage = () => {
    setIsImageModalOpen(true);
  };

  const submitImage = () => {
    if (imageUrl) {
      formatText('insertImage', imageUrl);
    }
    setIsImageModalOpen(false);
    setImageUrl('');
  };

  const handleDeleteNote = () => {
    if (!noteIdRef.current && !id) return;
    const noteIdToDelete = noteIdRef.current || id;
    
    setConfirmAction({
      title: 'Delete Note',
      message: 'Are you sure you want to delete this note? This action cannot be undone.',
      onConfirm: async () => {
        setConfirmAction(null);
        try {
          await api.delete(`/notes/${noteIdToDelete}`);
          navigate('/dashboard');
        } catch (error) {
          console.error("Failed to delete note", error);
        }
      }
    });
  };

  return (
    <div className="editor-page-container">
      {/* Top Action Bar */}
      <header className="editor-top-bar">
        <div className="breadcrumbs">
          <span>Workspace</span>
          <span className="divider-slash">/</span>
          <span>{workspaces.find(w => w.id === workspaceId)?.name || 'Personal'}</span>
          <span className="divider-slash">/</span>
          <span className="current-path">{note.title || (id ? 'Loading...' : 'New Draft')}</span>
        </div>

        <div className="editor-actions">
           <div className={`auto-saved-badge ${saveStatus}`}>
             {saveStatus === 'saved' && <><Check size={14} className="text-green" /> Auto-saved</>}
             {saveStatus === 'saving' && <span className="text-secondary">Saving...</span>}
             {saveStatus === 'error' && <span className="text-error">Save failed</span>}
           </div>
           
           <button 
             className="icon-btn hover-red"
             onClick={handleDeleteNote}
             title="Delete Note"
             disabled={!noteIdRef.current && !id}
           >
             <Trash2 size={18} />
           </button>
           
           <button 
             className={`icon-btn star-favorite-btn ${isFavorite ? 'is-fav' : ''}`}
             onClick={handleFavoriteToggle}
             title={isFavorite ? "Remove from favorites" : "Add to favorites"}
             disabled={!noteIdRef.current && !id}
           >
             <Star 
               size={20} 
               fill={isFavorite ? "#F59E0B" : "transparent"} 
               color={isFavorite ? "#F59E0B" : "currentColor"} 
             />
           </button>

           <div className="btn-group">
             <button className="btn-outline group-left" onClick={() => setIsExportOpen(true)}>
               Export <ChevronDown size={14} style={{ marginLeft: '4px' }} />
             </button>
             <button className="btn-primary group-right">
                Share
             </button>
           </div>
           
           <div className="avatar avatar-1" title={user?.name || 'User'}>
             {user?.name ? user.name.substring(0,2).toUpperCase() : 'US'}
           </div>
        </div>
      </header>

      <ExportModal 
        isOpen={isExportOpen} 
        onClose={() => setIsExportOpen(false)} 
        noteTitle={titleRef.current?.innerText || note.title || 'Untitled Note'}
        noteContent={editorRef.current?.innerHTML || note.content}
      />

      <div className="editor-main-layout">
        {/* Editor Area */}
        <div className="editor-canvas-wrapper">
          <div className="editor-canvas card">
            <h1 
              className="document-title" 
              contentEditable="true" 
              suppressContentEditableWarning
              ref={titleRef}
              onInput={handleInput}
            >
              Project Alpha Ideas
            </h1>
            
            <div className="document-metadata flex-wrap">
              <div className="meta-item">
                <Calendar size={14} /> {note.createdAt ? new Date(note.createdAt).toLocaleDateString() : new Date().toLocaleDateString()}
              </div>
              <div className="meta-item">
                <Clock size={14} /> {note.updatedAt ? new Date(note.updatedAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : 'Just now'}
              </div>
            </div>

            {/* Workspace & Section Selectors */}
            <div style={{display: 'flex', gap: '14px', margin: '16px 0 12px 0', flexWrap: 'wrap'}}>
              {/* Workspace Selector */}
              <div ref={wsDropdownRef} style={{position: 'relative'}}>
                <div className="editor-selector-card" onClick={() => { setIsWsDropdownOpen(!isWsDropdownOpen); setIsSectionDropdownOpen(false); }} style={{
                  display: 'flex', alignItems: 'center', gap: '10px',
                  padding: '10px 16px', borderRadius: '10px',
                  background: isWsDropdownOpen ? 'rgba(59,130,246,0.1)' : 'linear-gradient(135deg, rgba(59,130,246,0.06) 0%, rgba(59,130,246,0.02) 100%)',
                  border: '1px solid rgba(59,130,246,0.2)',
                  transition: 'all 0.2s ease', cursor: 'pointer', userSelect: 'none'
                }}>
                  <div style={{
                    width: '30px', height: '30px', borderRadius: '8px',
                    background: 'linear-gradient(135deg, #3B82F6, #2563EB)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    flexShrink: 0, boxShadow: '0 2px 6px rgba(59,130,246,0.3)'
                  }}>
                    <Folder size={14} color="#fff" />
                  </div>
                  <div style={{display: 'flex', flexDirection: 'column', gap: '1px', minWidth: '80px'}}>
                    <span style={{fontSize: '10px', fontWeight: '700', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.8px'}}>Workspace</span>
                    <span style={{fontSize: '14px', fontWeight: '600', color: 'var(--text-main)'}}>{workspaces.find(w => w.id === workspaceId)?.name || 'Select...'}</span>
                  </div>
                  <ChevronDown size={14} style={{color: 'var(--text-secondary)', transition: 'transform 0.2s', transform: isWsDropdownOpen ? 'rotate(180deg)' : 'none'}} />
                </div>

                {isWsDropdownOpen && (
                  <div style={{
                    position: 'absolute', top: 'calc(100% + 6px)', left: 0, minWidth: '220px',
                    background: 'var(--bg-surface)', borderRadius: '12px',
                    border: '1px solid var(--border-color)',
                    boxShadow: '0 8px 30px rgba(0,0,0,0.12), 0 2px 8px rgba(0,0,0,0.06)',
                    zIndex: 1000, padding: '6px', overflow: 'hidden',
                    animation: 'dropdownSlideIn 0.15s ease-out'
                  }}>
                    <div style={{padding: '8px 12px 6px', fontSize: '10px', fontWeight: '700', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.8px'}}>Select Workspace</div>
                    {workspaces.map(ws => (
                      <div
                        key={ws.id}
                        onClick={() => { setWorkspaceId(ws.id); setIsWsDropdownOpen(false); }}
                        className="custom-dropdown-item"
                        style={{
                          padding: '10px 12px', borderRadius: '8px', cursor: 'pointer',
                          display: 'flex', alignItems: 'center', gap: '10px',
                          background: ws.id === workspaceId ? 'rgba(59,130,246,0.08)' : 'transparent',
                          transition: 'background 0.15s'
                        }}
                      >
                        <div style={{
                          width: '8px', height: '8px', borderRadius: '50%',
                          background: ws.id === workspaceId ? '#3B82F6' : 'var(--border-color)',
                          flexShrink: 0, transition: 'background 0.15s'
                        }} />
                        <span style={{fontSize: '14px', fontWeight: ws.id === workspaceId ? '600' : '400', color: 'var(--text-main)'}}>{ws.name}</span>
                        {ws.id === workspaceId && <Check size={14} style={{marginLeft: 'auto', color: '#3B82F6'}} />}
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Section Selector */}
              <div ref={sectionDropdownRef} style={{position: 'relative'}}>
                <div className="editor-selector-card" onClick={() => { setIsSectionDropdownOpen(!isSectionDropdownOpen); setIsWsDropdownOpen(false); }} style={{
                  display: 'flex', alignItems: 'center', gap: '10px',
                  padding: '10px 16px', borderRadius: '10px',
                  background: isSectionDropdownOpen ? 'rgba(139,92,246,0.1)' : 'linear-gradient(135deg, rgba(139,92,246,0.06) 0%, rgba(139,92,246,0.02) 100%)',
                  border: '1px solid rgba(139,92,246,0.2)',
                  transition: 'all 0.2s ease', cursor: 'pointer', userSelect: 'none'
                }}>
                  <div style={{
                    width: '30px', height: '30px', borderRadius: '8px',
                    background: 'linear-gradient(135deg, #8B5CF6, #7C3AED)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    flexShrink: 0, boxShadow: '0 2px 6px rgba(139,92,246,0.3)'
                  }}>
                    <Tag size={14} color="#fff" />
                  </div>
                  <div style={{display: 'flex', flexDirection: 'column', gap: '1px', minWidth: '100px'}}>
                    <span style={{fontSize: '10px', fontWeight: '700', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.8px'}}>Section</span>
                    <span style={{fontSize: '14px', fontWeight: '600', color: tags.length > 0 ? 'var(--text-main)' : 'var(--text-secondary)'}}>
                      {tags.length > 0 ? tags.map(t => t.name).join(', ') : 'Select section...'}
                    </span>
                  </div>
                  <ChevronDown size={14} style={{color: 'var(--text-secondary)', transition: 'transform 0.2s', transform: isSectionDropdownOpen ? 'rotate(180deg)' : 'none'}} />
                </div>

                {isSectionDropdownOpen && (
                  <div style={{
                    position: 'absolute', top: 'calc(100% + 6px)', left: 0, minWidth: '240px',
                    background: 'var(--bg-surface)', borderRadius: '12px',
                    border: '1px solid var(--border-color)',
                    boxShadow: '0 8px 30px rgba(0,0,0,0.12), 0 2px 8px rgba(0,0,0,0.06)',
                    zIndex: 1000, padding: '6px', overflow: 'hidden',
                    animation: 'dropdownSlideIn 0.15s ease-out'
                  }}>
                    <div style={{padding: '8px 12px 6px', fontSize: '10px', fontWeight: '700', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.8px'}}>Add Section to Note</div>
                    {availableTags.length === 0 && (
                      <div style={{padding: '16px 12px', textAlign: 'center', color: 'var(--text-secondary)', fontSize: '13px'}}>No sections available. Create tags first.</div>
                    )}
                    {availableTags.map(tag => {
                      const isSelected = tags.some(t => t.id === tag.id);
                      return (
                        <div
                          key={tag.id}
                          onClick={async () => {
                            if (!noteIdRef.current) return;
                            try {
                              if (isSelected) {
                                await api.delete(`/notes/${noteIdRef.current}/tags/${tag.id}`);
                                setTags(tags.filter(t => t.id !== tag.id));
                              } else {
                                const res = await api.post(`/notes/${noteIdRef.current}/tags/${tag.id}`);
                                setTags(res.data.tags || []);
                              }
                            } catch (err) {
                              console.error('Failed to toggle tag', err);
                            }
                          }}
                          className="custom-dropdown-item"
                          style={{
                            padding: '10px 12px', borderRadius: '8px', cursor: 'pointer',
                            display: 'flex', alignItems: 'center', gap: '10px',
                            background: isSelected ? 'rgba(139,92,246,0.08)' : 'transparent',
                            transition: 'background 0.15s'
                          }}
                        >
                          <div style={{
                            width: '18px', height: '18px', borderRadius: '4px',
                            border: isSelected ? 'none' : '2px solid var(--border-color)',
                            background: isSelected ? 'linear-gradient(135deg, #8B5CF6, #7C3AED)' : 'transparent',
                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                            transition: 'all 0.15s', flexShrink: 0
                          }}>
                            {isSelected && <Check size={12} color="#fff" />}
                          </div>
                          <span style={{fontSize: '14px', fontWeight: isSelected ? '600' : '400', color: 'var(--text-main)'}}>{tag.name}</span>
                          <span style={{marginLeft: 'auto', fontSize: '11px', color: 'var(--text-secondary)', opacity: 0.7}}>#{tag.name}</span>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            </div>

            <style>{`
              .editor-selector-card:hover {
                transform: translateY(-1px);
                box-shadow: 0 4px 12px rgba(0,0,0,0.08);
              }
              .custom-dropdown-item:hover {
                background: var(--bg-hover) !important;
              }
              @keyframes dropdownSlideIn {
                from { opacity: 0; transform: translateY(-4px); }
                to { opacity: 1; transform: translateY(0); }
              }
            `}</style>

            <div className="document-tags">
              {tags.map(tag => (
                <span key={tag.id} className={`tag-pill bg-${tag.color || 'blue'}-light text-${tag.color || 'blue'}`}>
                  # {tag.name} <button className="remove-tag" onClick={() => handleRemoveTag(tag.id)}><X size={12} /></button>
                </span>
              ))}
              <button className="add-tag-text-btn" onClick={handleAddTagClick}>+ Add tag</button>
            </div>

            {/* Rich Text Toolbar */}
            <div className="rich-text-toolbar">
              <div className="toolbar-group">
                <button className="toolbar-btn" onClick={() => formatText('bold')} title="Bold"><Bold size={16} /></button>
                <button className="toolbar-btn" onClick={() => formatText('italic')} title="Italic"><Italic size={16} /></button>
                <button className="toolbar-btn" onClick={() => formatText('underline')} title="Underline"><Underline size={16} /></button>
              </div>
              <div className="toolbar-divider" />
              <div className="toolbar-group">
                <button className="toolbar-btn text-icon" onClick={() => formatText('formatBlock', 'H1')} title="Heading 1">H1</button>
                <button className="toolbar-btn text-icon" onClick={() => formatText('formatBlock', 'H2')} title="Heading 2">H2</button>
              </div>
              <div className="toolbar-divider" />
              <div className="toolbar-group">
                <button className="toolbar-btn" onClick={() => formatText('insertUnorderedList')} title="Bullet List"><List size={16} /></button>
                <button className="toolbar-btn" onClick={() => formatText('insertOrderedList')} title="Numbered List"><ListOrdered size={16} /></button>
                <button className="toolbar-btn" onClick={() => formatText('insertText', '[] ')} title="Add Checkbox"><CheckSquare size={16} /></button>
              </div>
              <div className="toolbar-group push-right">
                <button className="toolbar-btn" onClick={handleLink} title="Insert Link"><Link size={16} /></button>
                <button className="toolbar-btn" onClick={handleImage} title="Insert Image"><ImageIcon size={16} /></button>
              </div>
            </div>

            {/* Content Body Editable */}
            <div 
              className="editor-content-body" 
              contentEditable="true" 
              suppressContentEditableWarning
              ref={editorRef}
              onInput={handleInput}
              style={{ minHeight: '400px' }}
            >
              {/* Loaded dynamically by ref */}
            </div>
          </div>
        </div>

        {/* Right Sidebar - Editor Context */}
        <aside className="editor-context-sidebar">
           <div className="context-section">
             <h3 className="context-title">NOTE DETAILS</h3>
             <div className="detail-row">
               <span className="detail-label">Created</span>
               <span className="detail-val">{note.createdAt ? new Date(note.createdAt).toLocaleDateString() : 'Today'}</span>
             </div>
             <div className="detail-row">
               <span className="detail-label">Last Edited</span>
               <span className="detail-val">{note.updatedAt ? new Date(note.updatedAt).toLocaleTimeString() : 'Just now'}</span>
             </div>
             <div className="detail-row">
               <span className="detail-label">Author</span>
               <div className="author-val flex">
                 <div className="avatar micro avatar-2" style={{backgroundImage: 'none', backgroundColor: 'var(--primary-blue)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', fontSize: '10px'}}>
                   {note.owner?.name ? note.owner.name.substring(0,1) : (user?.name ? user.name.substring(0,1) : 'U')}
                 </div> 
                 {note.owner?.name || user?.name || 'Unknown'}
               </div>
             </div>
             <div className="detail-row">
               <span className="detail-label">Status</span>
               <span className="detail-val">{saveStatus === 'saved' ? 'Synced to Cloud' : 'Pending...'}</span>
             </div>
           </div>

           <div className="context-section">
             <h3 className="context-title">RELATED NOTES</h3>
             {relatedNotes.length === 0 ? (
               <p style={{fontSize: '13px', color: 'var(--text-secondary)'}}>No related notes found matching these tags.</p>
             ) : (
               <div style={{display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '8px'}}>
                 {relatedNotes.map(n => (
                   <div key={n.id} onClick={() => navigate(`/editor/${n.id}`)} style={{cursor: 'pointer', padding: '8px', background: 'var(--bg-surface)', borderRadius: '6px', fontSize: '13px', border: '1px solid var(--border-color)', display: 'flex', alignItems: 'center', gap: '8px'}}>
                     <FileText size={14} className="text-secondary" /> {n.title || 'Untitled'}
                   </div>
                 ))}
                <div 
                  className="add-tag-btn" 
                  onClick={handleAddTagClick}
                  style={{display: 'flex', alignItems: 'center', gap: '4px', color:'var(--text-secondary)', fontSize:'13px', cursor: 'pointer', padding: '4px 8px'}}
                >
                  <Plus size={14} /> Add tag
                </div>
              </div>
             )}
             <button className="link-note-btn text-blue bg-transparent" style={{marginTop: '12px'}} onClick={handleAddTagClick}>
               <Plus size={14} /> Link a note
             </button>
           </div>
        </aside>
      </div>

      {/* Custom Add Tag Modal */}
      {isTagModalOpen && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex', alignItems: 'center',
          justifyContent: 'center', zIndex: 99999
        }}>
          <div className="card" style={{
            background: 'var(--bg-surface)', width: '360px', borderRadius: '12px',
            padding: '24px', boxShadow: '0 8px 30px rgba(0,0,0,0.12)'
          }}>
            <h3 style={{margin: '0 0 16px 0', fontSize: '18px', color: 'var(--text-main)'}}>Add Tag</h3>
            <div className="form-group" style={{marginBottom: '12px'}}>
              <label style={{display: 'block', fontSize: '12px', fontWeight: 'bold', color: 'var(--text-secondary)', marginBottom: '8px'}}>TAG NAME</label>
              <input 
                autoFocus
                type="text" 
                placeholder="e.g. backend, meeting, urgent" 
                value={newTagName}
                onChange={(e) => setNewTagName(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && submitTag()}
                style={{
                  width: '100%', padding: '10px 12px', borderRadius: '8px',
                  border: '1px solid var(--border-color)', background: 'var(--bg-surface)',
                  color: 'var(--text-main)', outline: 'none'
                }}
              />
            </div>
            {availableTags.length > 0 && (
              <div style={{marginBottom: '16px'}}>
                <label style={{display: 'block', fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '6px'}}>EXISTING TAGS (click to add)</label>
                <div style={{display: 'flex', flexWrap: 'wrap', gap: '6px'}}>
                  {availableTags
                    .filter(t => !tags.some(et => et.id === t.id))
                    .map(t => (
                    <span 
                      key={t.id} 
                      onClick={() => { setNewTagName(t.name); }}
                      style={{
                        padding: '4px 10px', borderRadius: '12px', fontSize: '12px', cursor: 'pointer',
                        background: newTagName.toLowerCase() === t.name.toLowerCase() ? 'var(--primary-blue)' : 'var(--bg-surface)',
                        color: newTagName.toLowerCase() === t.name.toLowerCase() ? '#fff' : 'var(--text-secondary)',
                        border: '1px solid var(--border-color)'
                      }}
                    >
                      #{t.name}
                    </span>
                  ))}
                </div>
              </div>
            )}
            <div style={{display: 'flex', justifyContent: 'flex-end', gap: '12px'}}>
              <button className="btn-outline" onClick={() => { setIsTagModalOpen(false); setNewTagName(''); }}>Cancel</button>
              <button className="btn-primary" onClick={submitTag} disabled={!newTagName.trim() || isAddingTag}>
                {isAddingTag ? 'Adding...' : 'Add Tag'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Custom Insert Link Modal */}
      {isLinkModalOpen && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex', alignItems: 'center',
          justifyContent: 'center', zIndex: 99999
        }}>
          <div className="card" style={{
            background: 'var(--bg-surface)', width: '360px', borderRadius: '12px',
            padding: '24px', boxShadow: '0 8px 30px rgba(0,0,0,0.12)'
          }}>
            <h3 style={{margin: '0 0 16px 0', fontSize: '18px', color: 'var(--text-main)'}}>Insert Link</h3>
            <div className="form-group" style={{marginBottom: '20px'}}>
              <label style={{display: 'block', fontSize: '12px', fontWeight: 'bold', color: 'var(--text-secondary)', marginBottom: '8px'}}>URL</label>
              <input 
                autoFocus
                type="text" 
                placeholder="https://" 
                value={linkUrl}
                onChange={(e) => setLinkUrl(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && submitLink()}
                style={{
                  width: '100%', padding: '10px 12px', borderRadius: '8px',
                  border: '1px solid var(--border-color)', background: 'var(--bg-surface)',
                  color: 'var(--text-main)', outline: 'none'
                }}
              />
            </div>
            <div style={{display: 'flex', justifyContent: 'flex-end', gap: '12px'}}>
              <button className="btn-outline" onClick={() => { setIsLinkModalOpen(false); setLinkUrl(''); }}>Cancel</button>
              <button className="btn-primary" onClick={submitLink} disabled={!linkUrl.trim()}>Insert</button>
            </div>
          </div>
        </div>
      )}

      {/* Custom Insert Image Modal */}
      {isImageModalOpen && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex', alignItems: 'center',
          justifyContent: 'center', zIndex: 99999
        }}>
          <div className="card" style={{
            background: 'var(--bg-surface)', width: '360px', borderRadius: '12px',
            padding: '24px', boxShadow: '0 8px 30px rgba(0,0,0,0.12)'
          }}>
            <h3 style={{margin: '0 0 16px 0', fontSize: '18px', color: 'var(--text-main)'}}>Insert Image</h3>
            <div className="form-group" style={{marginBottom: '20px'}}>
              <label style={{display: 'block', fontSize: '12px', fontWeight: 'bold', color: 'var(--text-secondary)', marginBottom: '8px'}}>IMAGE URL</label>
              <input 
                autoFocus
                type="text" 
                placeholder="https://.../image.png" 
                value={imageUrl}
                onChange={(e) => setImageUrl(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && submitImage()}
                style={{
                  width: '100%', padding: '10px 12px', borderRadius: '8px',
                  border: '1px solid var(--border-color)', background: 'var(--bg-surface)',
                  color: 'var(--text-main)', outline: 'none'
                }}
              />
            </div>
            <div style={{display: 'flex', justifyContent: 'flex-end', gap: '12px'}}>
              <button className="btn-outline" onClick={() => { setIsImageModalOpen(false); setImageUrl(''); }}>Cancel</button>
              <button className="btn-primary" onClick={submitImage} disabled={!imageUrl.trim()}>Insert</button>
            </div>
          </div>
        </div>
      )}

      <ConfirmModal
        isOpen={!!confirmAction}
        title={confirmAction?.title}
        message={confirmAction?.message}
        onConfirm={confirmAction?.onConfirm}
        onCancel={() => setConfirmAction(null)}
      />
    </div>
  );
};

export default Editor;
