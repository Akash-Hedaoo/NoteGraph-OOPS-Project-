import React from 'react';
import { Outlet } from 'react-dom';

/* Since we need Sidebar and Topbar wrapping actual route content, 
   we construct a layout component wrapper */
import Sidebar from './Sidebar';
import TopBar from './TopBar';
import { Outlet as RouterOutlet } from 'react-router-dom';
import './DashboardLayout.css';

const DashboardLayout = () => {
  return (
    <div className="dashboard-layout">
      <Sidebar />
      <div className="main-content-area">
        <TopBar />
        <main className="page-content">
          <RouterOutlet />
        </main>
      </div>
    </div>
  );
};

export default DashboardLayout;
