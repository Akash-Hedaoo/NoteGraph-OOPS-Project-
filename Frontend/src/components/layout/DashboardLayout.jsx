import React, { useState } from 'react';

/* Since we need Sidebar and Topbar wrapping actual route content, 
   we construct a layout component wrapper */
import Sidebar from './Sidebar';
import TopBar from './TopBar';
import { Outlet as RouterOutlet, useLocation } from 'react-router-dom';
import './DashboardLayout.css';

const DashboardLayout = () => {
  const [isMobileSidebarOpen, setIsMobileSidebarOpen] = useState(false);
  const location = useLocation();

  // Close mobile sidebar when route changes
  React.useEffect(() => {
    setIsMobileSidebarOpen(false);
  }, [location.pathname]);

  return (
    <div className="dashboard-layout">
      {/* Mobile overlay backdrop */}
      {isMobileSidebarOpen && (
        <div 
          className="mobile-sidebar-overlay" 
          onClick={() => setIsMobileSidebarOpen(false)} 
        />
      )}
      
      <div className={`sidebar-wrapper ${isMobileSidebarOpen ? 'mobile-open' : ''}`}>
        <Sidebar />
      </div>
      
      <div className="main-content-area">
        <TopBar onMenuToggle={() => setIsMobileSidebarOpen(prev => !prev)} isMobileSidebarOpen={isMobileSidebarOpen} />
        <main className="page-content">
          <RouterOutlet />
        </main>
      </div>
    </div>
  );
};

export default DashboardLayout;
