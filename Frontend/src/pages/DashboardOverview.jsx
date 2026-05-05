import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, FileText, Edit3, Share2, Archive, Activity, Trash2 } from 'lucide-react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import ConfirmModal from '../components/ConfirmModal';
import './DashboardOverview.css';

const MetricCard = ({ title, value, trend, icon: Icon, trendUp }) => (
  <div className="metric-card card">
    <div className="metric-header">
      <span className="metric-title">{title}</span>
      <div className="metric-icon"><Icon size={18} /></div>
    </div>
    <div className="metric-content">
      <span className="metric-value">{value}</span>
      {trend && (
        <span className={`metric-trend ${trendUp ? 'positive' : 'neutral'}`}>
          {trendUp ? '↗ ' : ''}{trend}
        </span>
      )}
    </div>
  </div>
);

const ActivityItem = ({ user, action, target, time, isYou, type }) => (
  <div className="activity-item">
    <div className={`activity-dot dot-${type}`}></div>
    <div className="activity-content">
      <p className="activity-text">
        <span className="activity-user">{isYou ? 'You' : user}</span> {action} <span className="activity-target">{target}</span>
      </p>
      <span className="activity-time">{time}</span>
    </div>
  </div>
);

const DashboardOverview = () => {
  const navigate = useNavigate();
  const { user, workspaceId } = useAuth();
  const [notes, setNotes] = useState([]);
  const [activities, setActivities] = useState([]);
  const [stats, setStats] = useState({ totalNotes: 0, favNotes: 0 });
  const [loading, setLoading] = useState(true);
  const [confirmAction, setConfirmAction] = useState(null);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        if (!workspaceId || !user?.id) return;
        
        // Fetch recent notes for workspace
        const notesRes = await api.get(`/notes/workspace/${workspaceId}`);
        const allNotes = notesRes.data;
        setStats({
          totalNotes: allNotes.length,
          favNotes: allNotes.filter(n => n.favorite).length
        });
        setNotes(allNotes.slice(0, 3)); // Only show top 3 on dashboard
        
        // Fetch recent activities
        const activityRes = await api.get(`/activity/user/${user.id}`);
        setActivities(activityRes.data);
      } catch (err) {
        console.error("Failed to load dashboard data", err);
      } finally {
        setLoading(false);
      }
    };
    
    fetchDashboardData();
  }, [workspaceId, user]);

  const handleDeleteNote = (e, noteId) => {
    e.stopPropagation();
    setConfirmAction({
      title: 'Delete Note',
      message: 'Are you sure you want to delete this note? This action cannot be undone.',
      onConfirm: async () => {
        setConfirmAction(null);
        try {
          await api.delete(`/notes/${noteId}`);
          setNotes(notes.filter(n => n.id !== noteId));
          setStats(prev => ({
            ...prev,
            totalNotes: Math.max(0, prev.totalNotes - 1)
          }));
        } catch (err) {
          console.error("Failed to delete note", err);
        }
      }
    });
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    const now = new Date();
    const diffTime = Math.abs(now - date);
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays} days ago`;
    return date.toLocaleDateString();
  };

  const stripHtml = (html) => {
    if (!html) return '';
    let tmp = document.createElement("DIV");
    tmp.innerHTML = html;
    return tmp.textContent || tmp.innerText || "";
  };

  return (
    <div className="dashboard-overview">
      <header className="page-header">
        <h1 className="page-title">Dashboard Overview</h1>
        <p className="page-subtitle">Welcome back, {user?.name?.split(' ')[0] || 'User'}! Here's what's happening with your notes today.</p>
      </header>

      {/* Metrics Row */}
      <div className="metrics-grid">
        <MetricCard title="Total Notes" value={loading ? "..." : stats.totalNotes} icon={FileText} trendUp={true} />
        <MetricCard title="Recent Edits" value={loading ? "..." : activities.filter(a => a.action === 'edited').length} icon={Edit3} trendUp={true} />
        <MetricCard title="Favorites" value={loading ? "..." : stats.favNotes} icon={Share2} trendUp={true} />
        <MetricCard title="Archived" value="0" icon={Archive} />
      </div>

      <div className="dashboard-main-content">
        {/* Recent Notes Section */}
        <div className="recent-notes-section">
          <div className="section-header-flex">
            <h2>Recent Notes</h2>
            <a href="#" className="view-all-link">View All</a>
          </div>

          <div className="notes-grid">
            {notes.map(note => (
              <div 
                key={note.id} 
                className="note-card card" 
                style={{ cursor: 'pointer' }}
                onClick={() => navigate(`/editor/${note.id}`)}
              >
                <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                  {note.tags && note.tags.length > 0 ? (
                    <span className={`tag-badge bg-blue text-blue`}>{note.tags[0].name}</span>
                  ) : <span />}
                  <button 
                    className="icon-btn-small hover-red" 
                    onClick={(e) => handleDeleteNote(e, note.id)}
                    title="Delete Note"
                  >
                    <Trash2 size={14} />
                  </button>
                </div>
                <h3 className="note-title">{note.title || 'Untitled Note'}</h3>
                <p className="note-snippet">{stripHtml(note.content).substring(0, 80)}...</p>
                <div className="note-footer">
                  <div className="collaborators">
                    <div className="avatar micro avatar-1" />
                  </div>
                  <span className="note-time">Updated {formatDate(note.updatedAt)}</span>
                </div>
              </div>
            ))}

            <div 
              className="note-card create-new-card card" 
              onClick={() => navigate('/editor')}
            >
              <div className="create-center">
                <div className="add-icon-circle">
                  <Plus size={24} color="var(--primary-blue)" />
                </div>
                <span>Create New Note</span>
              </div>
            </div>
          </div>
        </div>

        {/* Activity Sidebar */}
        <aside className="activity-sidebar card">
          <h2 className="activity-title">Activity Summary</h2>
          <div className="activity-list">
            {activities.length > 0 ? (
              activities.map(log => (
                <ActivityItem 
                  key={log.id}
                  user={log.user.id === user.id ? 'You' : log.user.name} 
                  isYou={log.user.id === user.id} 
                  action={log.action} 
                  target={log.targetName} 
                  time={formatDate(log.createdAt)} 
                  type={log.action === 'edited' ? 'green' : 'blue'}
                />
              ))
            ) : (
              <p style={{color: 'var(--text-secondary)', fontSize: '13px'}}>No recent activity.</p>
            )}
          </div>
          <button className="btn-outline view-log-btn">View full history</button>
        </aside>
      </div>

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

export default DashboardOverview;
