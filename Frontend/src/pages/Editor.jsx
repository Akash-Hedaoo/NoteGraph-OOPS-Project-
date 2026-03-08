import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  Check, ChevronDown, Share, FileText, Calendar, Clock, Folder, 
  X, Bold, Italic, Underline, Heading1, Heading2, List, 
  ListOrdered, CheckSquare, Link, Image as ImageIcon, Sparkles, Plus, Star 
} from 'lucide-react';
import ExportModal from '../components/shared/ExportModal';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import './Editor.css';

const Editor = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { workspaceId, user } = useAuth();

  const [isExportOpen, setIsExportOpen] = useState(false);
  const [isFavorite, setIsFavorite] = useState(false);
  const [saveStatus, setSaveStatus] = useState('saved'); // 'saved', 'saving', 'error'
  
  const [note, setNote] = useState({ title: '', content: '' });
  const [tags, setTags] = useState([]);
  
  const editorRef = useRef(null);
  const titleRef = useRef(null);
  const saveTimeoutRef = useRef(null);
  const noteIdRef = useRef(id);

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
    const url = prompt('Enter link URL:');
    if (url) {
      formatText('createLink', url);
    }
  };

  const handleImage = () => {
    const url = prompt('Enter image URL:');
    if (url) {
      formatText('insertImage', url);
    }
  };

  return (
    <div className="editor-page-container">
      {/* Top Action Bar */}
      <header className="editor-top-bar">
        <div className="breadcrumbs">
          <span>Workspace</span>
          <span className="divider-slash">/</span>
          <span>{user?.name || 'Personal'}</span>
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
              <div className="meta-item">
                <Folder size={14} /> Uncategorized
              </div>
            </div>

            <div className="document-tags">
              {tags.map(tag => (
                <span key={tag.id} className="tag-pill bg-blue-light text-blue">
                  # {tag.name} <button className="remove-tag"><X size={12} /></button>
                </span>
              ))}
              <button className="add-tag-text-btn">+ Add tag</button>
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
             <p style={{fontSize: '13px', color: 'var(--text-secondary)'}}>No related notes found matching these tags.</p>
             <button className="link-note-btn text-blue bg-transparent" style={{marginTop: '12px'}}>
               <Plus size={14} /> Link a note
             </button>
           </div>
        </aside>
      </div>
    </div>
  );
};

export default Editor;
