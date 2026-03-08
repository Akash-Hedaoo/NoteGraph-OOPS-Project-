import React, { useState, useRef, useEffect } from 'react';
import { Search, Bell, Settings, HelpCircle, LogOut } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import SearchModal from '../shared/SearchModal';
import { useAuth } from '../../context/AuthContext';
import './TopBar.css';

const TopBar = () => {
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const navigate = useNavigate();
  const profileRef = useRef(null);
  const { user, logout } = useAuth();

  // Close profile dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (profileRef.current && !profileRef.current.contains(event.target)) {
        setIsProfileOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <header className="topbar">
      <div className="search-container" onClick={() => setIsSearchOpen(true)}>
        <Search className="search-icon" size={18} />
        <input 
          type="text" 
          placeholder="Search notes, tags, or folders..." 
          className="search-input"
          readOnly
        />
        <div className="search-shortcut">⌘K</div>
      </div>

      <SearchModal isOpen={isSearchOpen} onClose={() => setIsSearchOpen(false)} />

      <div className="topbar-actions">
        <button className="icon-btn">
          <HelpCircle size={20} />
        </button>
        <button className="icon-btn">
          <Settings size={20} />
        </button>
        <button className="icon-btn notification-btn">
          <Bell size={20} />
          <span className="badge"></span>
        </button>
          <div className="profile-dropdown-container" ref={profileRef}>
            <div 
              className="avatar topbar-avatar" 
              onClick={() => setIsProfileOpen(!isProfileOpen)}
              style={{ cursor: 'pointer' }}
            >
              {user?.name ? user.name.substring(0, 2).toUpperCase() : 'US'}
            </div>
            
            {isProfileOpen && (
              <div className="profile-dropdown-menu card">
                <div className="dropdown-header">
                  <span className="dropdown-name">{user?.name || 'User'}</span>
                  <span className="dropdown-email">{user?.email || 'user@example.com'}</span>
                </div>
                <div className="dropdown-divider"></div>
                <button 
                  className="dropdown-item text-red"
                  onClick={() => {
                    logout();
                    setIsProfileOpen(false);
                    navigate('/login');
                  }}
                >
                  <LogOut size={16} />
                  <span>Log out</span>
                </button>
              </div>
            )}
          </div>
      </div>
    </header>
  );
};

export default TopBar;
