import React, { useState, useRef, useEffect } from 'react';
import { Search, Settings, HelpCircle, LogOut, Moon, Sun } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import SearchModal from '../shared/SearchModal';
import { useAuth } from '../../context/AuthContext';
import './TopBar.css';

const TopBar = () => {
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [isDarkMode, setIsDarkMode] = useState(() => {
    return localStorage.getItem('theme') === 'dark';
  });
  const navigate = useNavigate();
  const profileRef = useRef(null);
  const settingsRef = useRef(null);
  const { user, logout } = useAuth();

  // Apply theme on mount and change
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', isDarkMode ? 'dark' : 'light');
    localStorage.setItem('theme', isDarkMode ? 'dark' : 'light');
  }, [isDarkMode]);

  // Close dropdowns when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (profileRef.current && !profileRef.current.contains(event.target)) {
        setIsProfileOpen(false);
      }
      if (settingsRef.current && !settingsRef.current.contains(event.target)) {
        setIsSettingsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const toggleTheme = () => {
    setIsDarkMode(prev => !prev);
  };

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
        <button className="icon-btn" title="Help" onClick={() => navigate('/guide')}>
          <HelpCircle size={20} />
        </button>

        {/* Settings with dropdown */}
        <div className="settings-dropdown-container" ref={settingsRef}>
          <button 
            className="icon-btn" 
            onClick={() => setIsSettingsOpen(!isSettingsOpen)}
          >
            <Settings size={20} />
          </button>
          
          {isSettingsOpen && (
            <div className="settings-dropdown-menu card">
              <div className="settings-dropdown-title">Settings</div>
              <div className="dropdown-divider"></div>
              <button className="dropdown-item" onClick={toggleTheme}>
                {isDarkMode ? <Sun size={16} /> : <Moon size={16} />}
                <span>{isDarkMode ? 'Light Mode' : 'Dark Mode'}</span>
                <span className="theme-badge">
                  {isDarkMode ? '☀️' : '🌙'}
                </span>
              </button>
            </div>
          )}
        </div>

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
