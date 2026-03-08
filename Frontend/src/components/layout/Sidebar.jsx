import React from 'react';
import { 
  FileText, Star, Tag, Trash2, FolderClosed, 
  Settings, User, Plus, LayoutDashboard
} from 'lucide-react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './Sidebar.css';

const Sidebar = () => {
  const navigate = useNavigate();
  const { user } = useAuth();

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
            <NavLink to="/tags" className="nav-item">
              <Tag size={18} /> Tags
            </NavLink>
            <NavLink to="/favorites" className="nav-item">
              <Star size={18} /> Favorites
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

      <div className="sidebar-footer">
        <div className="settings-btn">
          <Settings size={18} /> Settings
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
