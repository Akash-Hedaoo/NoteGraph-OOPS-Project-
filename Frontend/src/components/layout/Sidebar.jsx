import React, { useState, useRef, useEffect } from 'react';
import { 
  FileText, Star, Tag, Trash2, FolderClosed, BookOpen,
  Settings, User, Plus, LayoutDashboard, ChevronDown, Check, Edit2,
  Brain, LogOut, ClipboardCheck
} from 'lucide-react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import ConfirmModal from '../ConfirmModal';
import './Sidebar.css';

const Sidebar = () => {
  const navigate = useNavigate();
  const { user, workspaces, workspaceId, setWorkspaceId, fetchWorkspaces, logout } = useAuth();
  
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [newWorkspaceName, setNewWorkspaceName] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  const [isRenameModalOpen, setIsRenameModalOpen] = useState(false);
  const [renameWorkspaceId, setRenameWorkspaceId] = useState(null);
  const [renameWorkspaceName, setRenameWorkspaceName] = useState('');
  const [confirmAction, setConfirmAction] = useState(null);
  const dropdownRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleCreateWorkspace = async () => {
    if (!newWorkspaceName.trim()) return;
    setIsCreating(true);
    try {
      const res = await api.post('/workspaces', { name: newWorkspaceName });
      await fetchWorkspaces();
      setWorkspaceId(res.data.id);
      setIsCreateModalOpen(false);
      setNewWorkspaceName('');
      setIsDropdownOpen(false);
    } catch (e) {
      console.error("Failed to create workspace", e);
      alert("Failed to create workspace. Check console.");
    } finally {
      setIsCreating(false);
    }
  };
  
  const handleDeleteWorkspace = async (e, wId) => {
    e.stopPropagation();
    setConfirmAction({
      title: 'Delete Workspace',
      message: 'Are you sure you want to delete this workspace? All notes and tags inside will be permanently lost.',
      onConfirm: async () => {
        setConfirmAction(null);
        try {
          await api.delete(`/workspaces/${wId}`);
          await fetchWorkspaces();
          if (workspaceId === wId) {
            const remaining = workspaces.filter(w => w.id !== wId);
            if (remaining.length > 0) {
              setWorkspaceId(remaining[0].id);
            } else {
              setWorkspaceId('');
            }
          }
        } catch (error) {
          console.error("Failed to delete workspace", error);
        }
      }
    });
  };

  const openRenameModal = (e, ws) => {
    e.stopPropagation();
    setRenameWorkspaceId(ws.id);
    setRenameWorkspaceName(ws.name);
    setIsRenameModalOpen(true);
    setIsDropdownOpen(false);
  };

  const handleRenameWorkspace = async () => {
    if (!renameWorkspaceName.trim()) return;
    try {
      await api.put(`/workspaces/${renameWorkspaceId}`, { name: renameWorkspaceName.trim() });
      await fetchWorkspaces();
      setIsRenameModalOpen(false);
      setRenameWorkspaceId(null);
      setRenameWorkspaceName('');
    } catch (error) {
      console.error("Failed to rename workspace", error);
      alert("Failed to rename workspace.");
    }
  };

  const activeWorkspace = workspaces.find(w => w.id === workspaceId) || workspaces[0];

  return (
    <aside className="sidebar">
      {/* Brand & User Setup Area */}
      <div className="sidebar-header">
        <div className="brand">
          <div className="logo-icon-small">
            <svg viewBox="0 0 24 24" fill="var(--primary-blue)" xmlns="http://www.w3.org/2000/svg" width="20" height="20">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" />
              <path d="M2 17L12 22L22 17" stroke="var(--primary-blue)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="var(--primary-blue)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
          <span className="brand-text">NoteGraph</span>
        </div>
        
        <div className="user-profile-badge">
          <div className="avatar">
            <User size={16} />
          </div>
          <div className="user-info">
            <span className="user-name">{user?.name || 'User'}</span>
            <span className="user-plan">Pro Plan</span>
          </div>
        </div>

        <button 
          className="btn-primary create-note-btn"
          onClick={() => navigate('/editor')}
        >
          <Plus size={16} /> Create New Note
        </button>
      </div>

      <div className="sidebar-scrollable">
        {/* Custom Workspace Selector */}
        <div className="sidebar-section" style={{marginBottom: '10px', position: 'relative', zIndex: 50}} ref={dropdownRef}>
          <span className="section-title">MY WORKSPACE</span>
          <div style={{position: 'relative', marginTop: '6px'}}>
            <div 
              onClick={() => setIsDropdownOpen(!isDropdownOpen)}
              style={{
                width: '100%', padding: '10px 12px', borderRadius: '8px', 
                border: '1px solid var(--border-color)', background: 'var(--bg-surface)', 
                color: 'var(--text-main)', cursor: 'pointer', display: 'flex', 
                justifyContent: 'space-between', alignItems: 'center', fontSize: '14px',
                boxShadow: '0 1px 2px rgba(0,0,0,0.02)'
              }}
            >
              <span style={{fontWeight: '500'}}>{activeWorkspace?.name || 'Loading...'}</span>
              <ChevronDown size={16} className="text-secondary" style={{transform: isDropdownOpen ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform 0.2s'}} />
            </div>

            {isDropdownOpen && (
              <div style={{
                position: 'absolute', top: 'calc(100% + 4px)', left: 0, right: 0, 
                background: 'var(--bg-surface)', border: '1px solid var(--border-color)', 
                borderRadius: '8px', boxShadow: '0 4px 12px rgba(0,0,0,0.1)', 
                zIndex: 99999, overflow: 'hidden'
              }}>
                <div style={{maxHeight: '200px', overflowY: 'auto', padding: '4px'}}>
                  {workspaces.map(ws => (
                    <div 
                      key={ws.id} 
                      onClick={() => { setWorkspaceId(ws.id); setIsDropdownOpen(false); }}
                      style={{
                        padding: '8px 12px', cursor: 'pointer', borderRadius: '4px',
                        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                        background: workspaceId === ws.id ? 'var(--blue-light)' : 'transparent',
                        color: workspaceId === ws.id ? 'var(--primary-blue)' : 'var(--text-main)',
                        fontSize: '13px', fontWeight: workspaceId === ws.id ? '500' : '400'
                      }}
                      className="workspace-option group"
                    >
                      <span style={{display: 'flex', alignItems: 'center', gap: '8px'}}>
                        {ws.name}
                        {workspaceId === ws.id && <Check size={14} />}
                      </span>
                      {/* Don't allow deleting the Personal Workspace (this is usually the first created one or named Personal, but for now we let users delete any if backend allows, or we could hide it if ws.name === 'Personal Workspace') */}
                      {ws.name !== 'Personal Workspace' && (
                        <div style={{display: 'flex', alignItems: 'center', gap: '2px'}}>
                          <button 
                            onClick={(e) => openRenameModal(e, ws)} 
                            className="icon-btn-small"
                            style={{ opacity: 0.5 }}
                            title="Rename Workspace"
                          >
                            <Edit2 size={13} />
                          </button>
                          <button 
                            onClick={(e) => handleDeleteWorkspace(e, ws.id)} 
                            className="icon-btn-small hover-red"
                            style={{ opacity: 0.6 }}
                            title="Delete Workspace"
                          >
                            <Trash2 size={14} />
                          </button>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
                <div style={{borderTop: '1px solid var(--border-color)', padding: '4px'}}>
                  <div 
                    onClick={() => { setIsCreateModalOpen(true); setIsDropdownOpen(false); }}
                    style={{
                      padding: '8px 12px', cursor: 'pointer', borderRadius: '4px',
                      display: 'flex', alignItems: 'center', gap: '8px',
                      color: 'var(--text-secondary)', fontSize: '13px'
                    }}
                    className="workspace-option"
                  >
                    <Plus size={14} /> Create new workspace
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
        {/* Main Menu */}
        <div className="sidebar-section">
          <span className="section-title">MENU</span>
          <nav className="nav-menu">
            <NavLink to="/dashboard" className="nav-item">
              <LayoutDashboard size={18} /> Dashboard
            </NavLink>
            <NavLink to="/hierarchy" className="nav-item">
              <FolderClosed size={18} /> Note Graph
            </NavLink>
            <NavLink to="/knowledge-graph" className="nav-item">
              <Brain size={18} /> Knowledge Graph
            </NavLink>
            <NavLink to="/tags" className="nav-item">
              <Tag size={18} /> Tags
            </NavLink>
            <NavLink to="/favorites" className="nav-item">
              <Star size={18} /> Favorites
            </NavLink>
            <NavLink to="/revisions" className="nav-item">
              <ClipboardCheck size={18} /> Revisions
            </NavLink>
            <NavLink to="/guide" className="nav-item">
              <BookOpen size={18} /> Guide
            </NavLink>
          </nav>
        </div>

        {/* Custom Pinned section as per the mock */}
        <div className="sidebar-section">
          <div className="section-header">
            <span className="section-title">PINNED</span>
            <button className="icon-btn-small" title="Pin Note">
              <Plus size={14} />
            </button>
          </div>
          <nav className="nav-menu">
            <NavLink to="/editor" className="nav-item nested">
              <span className="dot dot-blue"></span> Scratchpad
            </NavLink>
            
            <NavLink to="/editor" className="nav-item nested">
              <span className="dot dot-purple"></span> Daily Log
            </NavLink>
          </nav>
        </div>
      </div>



      {/* Custom Create Workspace Modal */}
      {isCreateModalOpen && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex', alignItems: 'center',
          justifyContent: 'center', zIndex: 999999
        }}>
          <div className="card" style={{
            background: 'var(--bg-surface)', width: '360px', borderRadius: '12px',
            padding: '24px', boxShadow: '0 8px 30px rgba(0,0,0,0.12)'
          }}>
            <h3 style={{margin: '0 0 16px 0', fontSize: '18px', color: 'var(--text-main)'}}>Create New Workspace</h3>
            <p style={{margin: '0 0 16px 0', fontSize: '13px', color: 'var(--text-secondary)'}}>
              Workspaces help you separate different projects or contexts (e.g. Work vs Personal).
            </p>
            <div className="form-group" style={{marginBottom: '20px'}}>
              <label style={{display: 'block', fontSize: '12px', fontWeight: 'bold', color: 'var(--text-secondary)', marginBottom: '8px'}}>WORKSPACE NAME</label>
              <input 
                autoFocus
                type="text" 
                placeholder="e.g. Acme Corp Projects" 
                value={newWorkspaceName}
                onChange={(e) => setNewWorkspaceName(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleCreateWorkspace()}
                style={{
                  width: '100%', padding: '10px 12px', borderRadius: '8px',
                  border: '1px solid var(--border-color)', background: 'var(--bg-surface)',
                  color: 'var(--text-main)', outline: 'none'
                }}
              />
            </div>
            <div style={{display: 'flex', justifyContent: 'flex-end', gap: '12px'}}>
              <button 
                className="btn-outline" 
                onClick={() => { setIsCreateModalOpen(false); setNewWorkspaceName(''); }}
                disabled={isCreating}
              >
                Cancel
              </button>
              <button 
                className="btn-primary" 
                onClick={handleCreateWorkspace}
                disabled={!newWorkspaceName.trim() || isCreating}
              >
                {isCreating ? 'Creating...' : 'Create Workspace'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Rename Workspace Modal */}
      {isRenameModalOpen && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex', alignItems: 'center',
          justifyContent: 'center', zIndex: 999999
        }}>
          <div className="card" style={{
            background: 'var(--bg-surface)', width: '360px', borderRadius: '12px',
            padding: '24px', boxShadow: '0 8px 30px rgba(0,0,0,0.12)'
          }}>
            <h3 style={{margin: '0 0 16px 0', fontSize: '18px', color: 'var(--text-main)'}}>Rename Workspace</h3>
            <div className="form-group" style={{marginBottom: '20px'}}>
              <label style={{display: 'block', fontSize: '12px', fontWeight: 'bold', color: 'var(--text-secondary)', marginBottom: '8px'}}>NEW NAME</label>
              <input 
                autoFocus
                type="text" 
                placeholder="Enter new name" 
                value={renameWorkspaceName}
                onChange={(e) => setRenameWorkspaceName(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleRenameWorkspace()}
                style={{
                  width: '100%', padding: '10px 12px', borderRadius: '8px',
                  border: '1px solid var(--border-color)', background: 'var(--bg-surface)',
                  color: 'var(--text-main)', outline: 'none'
                }}
              />
            </div>
            <div style={{display: 'flex', justifyContent: 'flex-end', gap: '12px'}}>
              <button className="btn-outline" onClick={() => { setIsRenameModalOpen(false); setRenameWorkspaceName(''); }}>Cancel</button>
              <button className="btn-primary" onClick={handleRenameWorkspace} disabled={!renameWorkspaceName.trim()}>Rename</button>
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
    </aside>
  );
};

export default Sidebar;
