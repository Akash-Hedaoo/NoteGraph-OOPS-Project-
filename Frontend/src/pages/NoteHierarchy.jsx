import React, { useState, useEffect } from 'react';
import { 
  ChevronRight, Filter, ArrowUpDown, FileText, Share2, Download,
  Settings, Maximize, Minimize, X, Activity, User, Tag, ChevronDown, Plus, Trash2, Edit2
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import ConfirmModal from '../components/ConfirmModal';
import './NoteHierarchy.css';

const COLORS = ['purple', 'blue', 'green', 'orange', 'red'];

// Component for a Node in the Tag hierarchy
const GraphNode = ({ title, type = 'note', tagColor, count, isRoot, onClick }) => {
  if (type === 'tag') {
    return (
      <div className={`org-node tag-node border-${tagColor}`} onClick={onClick} style={{ cursor: 'pointer' }}>
        <div className={`node-header bg-${tagColor}`}>
          <Tag size={14} className="node-icon" />
          <span className="node-title">#{title}</span>
        </div>
        <div className="node-body">
           <span className="node-count">{count} connected notes</span>
        </div>
      </div>
    );
  }

  // Note type node
  return (
    <div className={`org-node note-node ${isRoot ? 'root-node' : ''}`}>
      <div className="node-body">
        <FileText size={16} className={`text-${tagColor}`} />
        <span className="node-title">{title}</span>
      </div>
    </div>
  );
};

const NoteHierarchy = () => {
  const navigate = useNavigate();
  const { workspaceId, workspaces, user, fetchWorkspaces } = useAuth();
  const [graphData, setGraphData] = useState([]);
  const [selectedTag, setSelectedTag] = useState(null);
  const [loading, setLoading] = useState(true);
  const [viewMode, setViewMode] = useState('tree'); // 'org' or 'tree'
  const [expandedTags, setExpandedTags] = useState({});

  // Feature states
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [filterTagId, setFilterTagId] = useState(null);

  const filteredGraphData = filterTagId 
    ? graphData.filter(tag => tag.tagId === filterTagId)
    : graphData;

  // Rename modal state
  const [isRenameModalOpen, setIsRenameModalOpen] = useState(false);
  const [renameTarget, setRenameTarget] = useState(null); // { type: 'section'|'workspace', id, currentName }
  const [renameName, setRenameName] = useState('');
  const [confirmAction, setConfirmAction] = useState(null);

  useEffect(() => {
    const fetchGraph = async () => {
      if (!workspaceId) return;
      try {
        const response = await api.get(`/graph/${workspaceId}`);
        // The endpoint returns a GraphDto with 'branches' array
        setGraphData(response.data.branches || []);
        if (response.data.branches && response.data.branches.length > 0) {
          setSelectedTag(response.data.branches[0]);
          // Auto-expand all tags by default
          const expandState = {};
          response.data.branches.forEach(b => expandState[b.tagId] = true);
          setExpandedTags(expandState);
        }
      } catch (err) {
        console.error("Failed to fetch graph data", err);
      } finally {
        setLoading(false);
      }
    };

    fetchGraph();
  }, [workspaceId]);

  const toggleTagExpand = (tagId, e) => {
    if (e) e.stopPropagation();
    setExpandedTags(prev => ({
      ...prev,
      [tagId]: !prev[tagId]
    }));
  };

  const fetchGraph = async () => {
    if (!workspaceId) return;
    try {
      const response = await api.get(`/graph/${workspaceId}`);
      setGraphData(response.data.branches || []);
    } catch (err) {
      console.error("Failed to refresh graph", err);
    }
  };

  const handleDeleteSection = (e, tagId) => {
    e.stopPropagation();
    setConfirmAction({
      title: 'Delete Section',
      message: 'Are you sure you want to delete this section (tag)? Notes will be unlinked but not deleted.',
      onConfirm: async () => {
        setConfirmAction(null);
        try {
          await api.delete(`/tags/${tagId}`);
          fetchGraph();
          if (selectedTag && selectedTag.tagId === tagId) setSelectedTag(null);
        } catch (error) {
          console.error("Failed to delete section", error);
        }
      }
    });
  };

  const handleDeleteNote = (e, noteId) => {
    e.stopPropagation();
    setConfirmAction({
      title: 'Delete Note',
      message: 'Are you sure you want to delete this note? This action cannot be undone.',
      onConfirm: async () => {
        setConfirmAction(null);
        try {
          await api.delete(`/notes/${noteId}`);
          fetchGraph();
        } catch (error) {
          console.error("Failed to delete note", error);
        }
      }
    });
  };

  const openRenameModal = (e, type, id, currentName) => {
    e.stopPropagation();
    setRenameTarget({ type, id, currentName });
    setRenameName(currentName);
    setIsRenameModalOpen(true);
  };

  const handleRename = async () => {
    if (!renameName.trim() || !renameTarget) return;
    try {
      if (renameTarget.type === 'section') {
        await api.put(`/tags/${renameTarget.id}`, { name: renameName.trim(), color: 'blue' });
        fetchGraph();
      } else if (renameTarget.type === 'workspace') {
        await api.put(`/workspaces/${renameTarget.id}`, { name: renameName.trim() });
        await fetchWorkspaces();
      }
      setIsRenameModalOpen(false);
      setRenameTarget(null);
      setRenameName('');
    } catch (error) {
      console.error('Failed to rename', error);
      alert('Failed to rename. Please try again.');
    }
  };

  const handleExportWorkspace = (e) => {
    e.stopPropagation();
    const wsName = workspaces.find(w => w.id === workspaceId)?.name || 'Workspace';
    let allNotes = [];
    graphData.forEach(tag => {
      if (tag.leaves) {
        tag.leaves.forEach(note => allNotes.push({ ...note, section: tag.tagTitle }));
      }
    });
    const htmlContent = `<!DOCTYPE html><html><head><meta charset="utf-8"><title>${wsName} - Exported Notes</title><style>body{max-width:800px;margin:0 auto;padding:2rem;font-family:'Segoe UI',sans-serif;color:#222;line-height:1.6}h1{font-size:24px;color:#111;border-bottom:2px solid #3B82F6;padding-bottom:12px}h2{font-size:18px;color:#3B82F6;margin-top:32px}h3{font-size:15px;margin:16px 0 8px}.note-card{background:#f9fafb;border:1px solid #e5e7eb;border-radius:8px;padding:16px;margin:12px 0}.section-label{display:inline-block;background:#EEF2FF;color:#4F46E5;padding:2px 10px;border-radius:12px;font-size:12px;font-weight:600;margin-bottom:8px}</style></head><body><h1>📁 ${wsName}</h1><p style="color:#666">Exported on ${new Date().toLocaleDateString()} • ${allNotes.length} notes across ${graphData.length} sections</p>`
      + graphData.map(tag => `<h2>🏷️ ${tag.tagTitle} (${tag.noteCount} notes)</h2>` + (tag.leaves || []).map(note => `<div class="note-card"><h3>${note.title || 'Untitled Note'}</h3>${note.content || '<p style="color:#999">No content</p>'}</div>`).join('')).join('')
      + `</body></html>`;
    const blob = new Blob([htmlContent], { type: 'text/html' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${wsName.replace(/[^a-z0-9]/gi, '_').toLowerCase()}_export.html`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const handleExportSection = (e, tagNode) => {
    e.stopPropagation();
    const htmlContent = `<!DOCTYPE html><html><head><meta charset="utf-8"><title>${tagNode.tagTitle} - Exported Notes</title><style>body{max-width:800px;margin:0 auto;padding:2rem;font-family:'Segoe UI',sans-serif;color:#222;line-height:1.6}h1{font-size:24px;color:#111;border-bottom:2px solid #8B5CF6;padding-bottom:12px}h3{font-size:15px;margin:16px 0 8px}.note-card{background:#f9fafb;border:1px solid #e5e7eb;border-radius:8px;padding:16px;margin:12px 0}</style></head><body><h1>🏷️ ${tagNode.tagTitle}</h1><p style="color:#666">Exported on ${new Date().toLocaleDateString()} • ${tagNode.noteCount} notes</p>`
      + (tagNode.leaves || []).map(note => `<div class="note-card"><h3>${note.title || 'Untitled Note'}</h3>${note.content || '<p style="color:#999">No content</p>'}</div>`).join('')
      + `</body></html>`;
    const blob = new Blob([htmlContent], { type: 'text/html' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${tagNode.tagTitle.replace(/[^a-z0-9]/gi, '_').toLowerCase()}_section_export.html`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  return (
    <div className="note-hierarchy-page">
      <div className={`hierarchy-main-area ${isFullscreen ? 'fullscreen-mode' : ''}`}>
        {/* Header Breadcrumbs */}
        <header className="page-header-row">
          <div className="breadcrumbs">
            <span>Workspaces</span>
            <ChevronRight size={14} className="text-secondary" />
            <span className="current-path">{workspaces.find(w => w.id === workspaceId)?.name || 'Workspace'}</span>
          </div>
          
          <div className="header-actions-flex">
            <h1 className="page-title">Note Connections</h1>
            <div className="toolbar-actions">
               <div className="view-toggles card">
                 <button className={`toggle-btn ${viewMode === 'org' ? 'active' : ''}`} title="Org Chart View" onClick={() => setViewMode('org')}>
                   <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="8" y="3" width="8" height="6" rx="1"/><rect x="2" y="15" width="8" height="6" rx="1"/><rect x="14" y="15" width="8" height="6" rx="1"/><path d="M12 9v2"/><path d="M6 15v-2a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v2"/></svg>
                 </button>
                 <button className={`toggle-btn ${viewMode === 'tree' ? 'active' : ''}`} title="Tree List View" onClick={() => setViewMode('tree')}>
                   <FileText size={18} />
                 </button>
               </div>
               <button className="btn-outline" onClick={handleExportWorkspace} title="Export entire workspace">
                 <Download size={16} /> Export
               </button>
               <div style={{position: 'relative'}}>
                 <button className="btn-outline" onClick={() => setIsFilterOpen(!isFilterOpen)}>
                   <Filter size={16} /> {filterTagId ? `#${graphData.find(t => t.tagId === filterTagId)?.tagTitle}` : 'Filter by Tag'}
                 </button>
                 {isFilterOpen && (
                   <div className="card" style={{position: 'absolute', top: '100%', right: 0, marginTop: '8px', padding: '8px', zIndex: 100, background: 'var(--bg-surface)', border: '1px solid var(--border-color)', borderRadius: '8px', minWidth: '160px', boxShadow: '0 4px 12px rgba(0,0,0,0.1)'}}>
                     <div 
                       style={{padding: '8px 12px', cursor: 'pointer', borderRadius: '4px', fontSize: '13px', background: filterTagId === null ? 'var(--bg-app)' : 'transparent', color: filterTagId === null ? 'var(--primary-blue)' : 'var(--text-primary)'}}
                       onClick={() => { setFilterTagId(null); setIsFilterOpen(false); }}
                     >
                       All Tags
                     </div>
                     {graphData.map(tag => (
                       <div 
                         key={tag.tagId}
                         style={{padding: '8px 12px', cursor: 'pointer', borderRadius: '4px', fontSize: '13px', background: filterTagId === tag.tagId ? 'var(--bg-app)' : 'transparent', color: filterTagId === tag.tagId ? 'var(--primary-blue)' : 'var(--text-primary)'}}
                         onClick={() => { setFilterTagId(tag.tagId); setIsFilterOpen(false); }}
                       >
                         #{tag.tagTitle}
                       </div>
                     ))}
                   </div>
                 )}
               </div>
               <button className="icon-btn-small border-btn" onClick={() => setIsFullscreen(!isFullscreen)}>
                 {isFullscreen ? <Minimize size={16} /> : <Maximize size={16} />}
               </button>
            </div>
          </div>
        </header>

        {/* View Areas */}
        <div className={`hierarchy-canvas card ${viewMode === 'org' ? 'org-chart-canvas' : 'tree-canvas'}`}>

          {viewMode === 'org' && (
            <div className="org-tree">
              {/* Root Note Level */}
              <div className="org-level">
                <GraphNode title={`${workspaces.find(w => w.id === workspaceId)?.name || 'My'} Workspace Notes`} isRoot={true} tagColor="blue" />
              </div>

              {/* Connecting Line Down from Root */}
              {filteredGraphData.length > 0 && <div className="org-line-down"></div>}

              {/* Horizontal Connecting Line for Branches */}
              {filteredGraphData.length > 1 && <div className="org-line-horizontal" style={{ width: `${Math.min(filteredGraphData.length * 200, 800)}px` }}></div>}

              {/* Tag Categories Level */}
              <div className="org-level branches">
                
                {loading ? (
                  <div style={{ padding: '20px', color: 'var(--text-secondary)' }}>Loading graph...</div>
                ) : filteredGraphData.length === 0 ? (
                  <div style={{ padding: '20px', color: 'var(--text-secondary)' }}>No tags found.</div>
                ) : (
                  filteredGraphData.map((tagNode, index) => {
                    const color = tagNode.tagColor || COLORS[index % COLORS.length];
                    return (
                      <div key={tagNode.tagId} className="org-branch">
                        <div className="org-line-down short"></div>
                        <GraphNode 
                          type="tag" 
                          title={tagNode.tagTitle} 
                          tagColor={color} 
                          count={tagNode.noteCount} 
                          onClick={() => navigate(`/editor/${note.id}`)}
                        />
                        <div className="org-line-down short"></div>
                        
                        <div className="org-leaves">
                          {tagNode.leaves && tagNode.leaves.map((note, noteIdx) => (
                            <React.Fragment key={note.id}>
                              <GraphNode title={note.title || 'Untitled Note'} tagColor={color} />
                              {noteIdx < tagNode.leaves.length - 1 && <div className="leaf-connector"></div>}
                            </React.Fragment>
                          ))}
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </div>
          )}

          {viewMode === 'tree' && (
             <div className="tree-list-container" style={{padding: '20px', width: '100%', maxWidth: '800px', margin: '0 auto'}}>
               <div className="tree-root card" style={{marginBottom: '30px', padding: '16px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', border: '1px solid var(--border-color)', borderRadius: '8px'}}>
                  <div style={{display: 'flex', alignItems: 'center', gap: '12px'}}>
                    <div className="folder-icon" style={{background: 'var(--blue-light)', color: 'var(--primary-blue)', padding: '8px', borderRadius: '6px'}}>
                       <span style={{fontWeight: 'bold', fontSize: '18px'}}>W</span>
                    </div>
                    <div>
                      <h3 style={{margin: 0, fontSize: '16px'}}>{workspaces.find(w => w.id === workspaceId)?.name || 'My'} Workspace</h3>
                      <p style={{margin: '4px 0 0 0', fontSize: '13px', color: 'var(--text-secondary)'}}>Tag hierarchy and note organization</p>
                    </div>
                  </div>
                  <div style={{display: 'flex', alignItems: 'center', gap: '4px'}}>
                    <button 
                      className="icon-btn-small" 
                      onClick={(e) => openRenameModal(e, 'workspace', workspaceId, workspaces.find(w => w.id === workspaceId)?.name || '')}
                      title="Rename Workspace"
                    >
                      <Edit2 size={14} />
                    </button>
                  </div>
               </div>

                {loading ? (
                  <div style={{ padding: '20px', color: 'var(--text-secondary)' }}>Loading graph...</div>
                ) : filteredGraphData.length === 0 ? (
                  <div style={{ padding: '20px', color: 'var(--text-secondary)' }}>No tags found.</div>
                ) : (
                  <div className="tree-branches" style={{display: 'flex', flexDirection: 'column', gap: '16px', borderLeft: '1px solid var(--border-color)', marginLeft: '24px', paddingLeft: '24px'}}>
                    {filteredGraphData.map((tagNode, index) => {
                      const color = tagNode.tagColor || COLORS[index % COLORS.length];
                      const isExpanded = expandedTags[tagNode.tagId];
                      return (
                        <div key={tagNode.tagId} className="tree-branch" style={{position: 'relative'}}>
                           {/* Connecting horizontal line */}
                           <div style={{position: 'absolute', left: '-24px', top: '24px', width: '24px', height: '1px', background: 'var(--border-color)'}}></div>

                           {/* Tag Header */}
                           <div className="tree-tag-header card" style={{padding: '16px 20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', border: '1px solid var(--border-color)', borderRadius: '12px', cursor: 'pointer', background: 'var(--bg-surface)', boxShadow: '0 2px 8px rgba(0,0,0,0.02)'}} onClick={() => { setSelectedTag({...tagNode, color}); toggleTagExpand(tagNode.tagId); }}>
                             <div style={{display: 'flex', alignItems: 'center', gap: '16px'}}>
                               <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{color: '#ccc'}}><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
                               <div style={{display: 'flex', flexDirection: 'column'}}>
                                   <div style={{display: 'flex', alignItems: 'center', gap: '8px'}}>
                                      <span style={{display: 'inline-block', width:'10px', height:'10px', borderRadius:'50%', background: `var(--${color}-main)`}}></span>
                                      <h4 style={{margin: 0, fontSize: '16px', fontWeight: '600', color: 'var(--text-main)'}}>{tagNode.tagTitle}</h4>
                                   </div>
                                   <span style={{fontSize: '13px', color: 'var(--text-secondary)', marginTop: '4px', marginLeft: '18px'}}>{tagNode.noteCount} sub-notes</span>
                               </div>
                             </div>
                             <div style={{display: 'flex', alignItems: 'center', gap: '4px'}}>
                               <button className="icon-btn-small" onClick={(e) => openRenameModal(e, 'section', tagNode.tagId, tagNode.tagTitle)} title="Rename Section">
                                 <Edit2 size={13} />
                               </button>
                               <button className="icon-btn-small hover-red" onClick={(e) => handleDeleteSection(e, tagNode.tagId)} title="Delete Section">
                                 <Trash2 size={14} />
                               </button>
                                <button className="icon-btn-small" onClick={(e) => handleExportSection(e, tagNode)} title="Export Section">
                                  <Download size={13} />
                                </button>
                               <button className="icon-btn-small" style={{transform: isExpanded ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform 0.2s'}}>
                                 <ChevronDown size={18} />
                               </button>
                             </div>
                           </div>

                           {/* Notes underneath */}
                           {isExpanded && tagNode.leaves && (
                             <div className="tree-leaves" style={{display: 'flex', flexDirection: 'column', gap: '8px', borderLeft: '1px solid var(--border-color)', marginLeft: '24px', paddingLeft: '24px', marginTop: '12px', paddingBottom: '12px'}}>
                               {tagNode.leaves.map((note) => (
                                 <div key={note.id} className="tree-leaf card" style={{padding: '14px 20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', border: '1px solid var(--border-color)', borderRadius: '8px', background: 'var(--bg-surface)', boxShadow: '0 1px 3px rgba(0,0,0,0.02)'}} onClick={() => navigate(`/editor/${note.id}`)}>
                                    <div style={{display: 'flex', alignItems: 'center', gap: '12px'}}>
                                      <FileText size={16} className={`text-${color}`} />
                                      <span style={{fontSize: '14px', fontWeight: '500'}}>{note.title || 'Untitled Note'}</span>
                                    </div>
                                    <div style={{display: 'flex', alignItems: 'center', gap: '6px'}}>
                                      <span style={{fontSize: '11px', padding: '2px 8px', background: 'var(--bg-surface)', borderRadius: '12px', border: '1px solid var(--border-color)', color: 'var(--text-secondary)'}}>Linked</span>
                                      <button className="icon-btn-small hover-red" onClick={(e) => handleDeleteNote(e, note.id)} title="Delete Note">
                                        <Trash2 size={13} />
                                      </button>
                                    </div>
                                 </div>
                               ))}
                               <div className="add-leaf-dropzone" style={{border: '1px dashed var(--primary-blue)', background: 'var(--tag-blue-bg)', color: 'var(--primary-blue)', padding: '12px', textAlign: 'center', borderRadius: '8px', fontSize: '13px', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px', cursor: 'pointer', marginTop: '4px'}}>
                                  <Plus size={14} /> Drop here to move
                               </div>
                             </div>
                           )}
                        </div>
                      )
                    })}
                    <div className="add-new-section" style={{border: '1px dashed var(--border-color)', color: 'var(--text-secondary)', padding: '16px', textAlign: 'center', borderRadius: '8px', fontSize: '14px', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px', cursor: 'pointer', background: 'var(--bg-surface)', marginTop: '8px'}}>
                      <Plus size={16} /> Add new tag section
                    </div>
                  </div>
                )}
             </div>
          )}
        </div>
      </div>

      {/* Right Sidebar - Connection Details */}
      {selectedTag && (
        <aside className="note-details-sidebar">
          <div className="details-header">
            <h3>Connection Details</h3>
            <button className="icon-btn-small" onClick={() => setSelectedTag(null)}><X size={18} /></button>
          </div>

          <div className={`preview-card bg-blue-tint`}>
            <div className={`tag-badge-large bg-${selectedTag.color || 'blue'}`}>
              <Tag size={20} color="var(--bg-surface)" />
            </div>
            <h4 className={`mt-4 text-${selectedTag.color || 'blue'}-dark`}>#{selectedTag.tagName || selectedTag.tagTitle}</h4>
            <span className="preview-path">{selectedTag.noteCount} Notes Connected</span>
            <svg className="watermark-icon" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round"><path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
          </div>

          <div className="details-section">
            <div className="section-title-flex">
               <span>GRAPH STATS</span>
            </div>
            
            <div className="stat-grid-small">
              <div className="stat-box">
                <span className="stat-num">{selectedTag.noteCount}</span>
                <span className="stat-lbl">Notes</span>
              </div>
              <div className="stat-box">
                <span className="stat-num">{selectedTag.noteCount * 2}</span>
                <span className="stat-lbl">Links</span>
              </div>
              <div className="stat-box">
                <span className="stat-num">High</span>
                <span className="stat-lbl">Density</span>
              </div>
            </div>
          </div>

          <div className="details-section">
            <div className="section-title-flex">
              <span>CONNECTED NOTES (#{selectedTag.tagName})</span>
            </div>
            
            <div className="connected-notes-list">
              {selectedTag.leaves && selectedTag.leaves.map(note => (
                <div key={note.id} className="mini-note-result card">
                  <div className={`icon-box bg-${selectedTag.color || 'blue'}-light text-${selectedTag.color || 'blue'}`}>
                    <FileText size={16} />
                  </div>
                  <div className="c-info">
                     <span className="c-title">{note.title || 'Untitled Note'}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </aside>
      )}

      {/* Rename Modal */}
      {isRenameModalOpen && renameTarget && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex', alignItems: 'center',
          justifyContent: 'center', zIndex: 99999
        }}>
          <div className="card" style={{
            background: 'var(--bg-surface)', width: '360px', borderRadius: '12px',
            padding: '24px', boxShadow: '0 8px 30px rgba(0,0,0,0.12)'
          }}>
            <h3 style={{margin: '0 0 16px 0', fontSize: '18px', color: 'var(--text-main)'}}>
              Rename {renameTarget.type === 'section' ? 'Section' : 'Workspace'}
            </h3>
            <div className="form-group" style={{marginBottom: '20px'}}>
              <label style={{display: 'block', fontSize: '12px', fontWeight: 'bold', color: 'var(--text-secondary)', marginBottom: '8px'}}>NEW NAME</label>
              <input 
                autoFocus
                type="text" 
                placeholder="Enter new name" 
                value={renameName}
                onChange={(e) => setRenameName(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleRename()}
                style={{
                  width: '100%', padding: '10px 12px', borderRadius: '8px',
                  border: '1px solid var(--border-color)', background: 'var(--bg-surface)',
                  color: 'var(--text-main)', outline: 'none'
                }}
              />
            </div>
            <div style={{display: 'flex', justifyContent: 'flex-end', gap: '12px'}}>
              <button className="btn-outline" onClick={() => { setIsRenameModalOpen(false); setRenameName(''); }}>Cancel</button>
              <button className="btn-primary" onClick={handleRename} disabled={!renameName.trim()}>Rename</button>
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

export default NoteHierarchy;
