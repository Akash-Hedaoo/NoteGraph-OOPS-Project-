import React, { useState, useEffect } from 'react';
import { Sparkles, CheckCircle, Clock, XCircle, Trash2 } from 'lucide-react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import './RevisionPage.css';

const RevisionPage = () => {
  const { user, workspaceId } = useAuth();
  const [planItems, setPlanItems] = useState([]);
  const [scheduledRevisions, setScheduledRevisions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState({ text: '', type: '' });
  const [scheduleDates, setScheduleDates] = useState({});

  useEffect(() => {
    if (workspaceId && user?.id) loadScheduledRevisions();
  }, [workspaceId, user]);

  const loadScheduledRevisions = async () => {
    try {
      const res = await api.get(`/revisions/workspace/${workspaceId}/user/${user.id}`);
      setScheduledRevisions(res.data);
    } catch (err) {
      console.error('Failed to load revisions', err);
    }
  };

  const generatePlan = async () => {
    setLoading(true);
    setStatus({ text: 'Generating AI revision plan... This may take a moment.', type: 'loading' });
    try {
      const res = await api.post(`/ai/revision-plan/${workspaceId}`, {});
      setPlanItems(res.data);
      setStatus({ text: `Plan generated for ${res.data.length} notes`, type: 'success' });
    } catch (err) {
      setStatus({ text: 'Failed to generate plan: ' + (err.response?.data?.error || err.message), type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const scheduleRevision = async (item) => {
    const dateStr = scheduleDates[item.noteId];
    if (!dateStr) {
      setStatus({ text: 'Please select a date/time first', type: 'error' });
      return;
    }
    try {
      await api.post('/revisions', {
        noteId: item.noteId,
        userId: user.id,
        scheduledAt: dateStr,
        complexityScore: item.complexity,
        aiReason: item.reason
      });
      setStatus({ text: `Revision scheduled for "${item.title}"`, type: 'success' });
      // Remove scheduled note from the plan
      setPlanItems(prev => prev.filter(p => p.noteId !== item.noteId));
      loadScheduledRevisions();
    } catch (err) {
      setStatus({ text: 'Failed to schedule: ' + err.message, type: 'error' });
    }
  };

  const updateStatus = async (id, newStatus) => {
    try {
      await api.put(`/revisions/${id}/status`, { status: newStatus });
      // Keep in local state for the duration of the session
      setScheduledRevisions(prev => prev.map(rev => 
        rev.id === id ? { ...rev, status: newStatus } : rev
      ));
    } catch (err) {
      console.error('Failed to update', err);
    }
  };

  const deleteRevision = async (id) => {
    try {
      await api.delete(`/revisions/${id}`);
      loadScheduledRevisions();
    } catch (err) {
      console.error('Failed to delete', err);
    }
  };

  const getComplexityClass = (c) => c <= 3 ? 'low' : c <= 6 ? 'medium' : 'high';

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    try {
      return new Date(dateStr).toLocaleString('en-US', {
        month: 'short', day: 'numeric', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
      });
    } catch { return dateStr; }
  };

  const getStatusIcon = (s) => {
    switch (s) {
      case 'SENT': return '✉️';
      case 'COMPLETED': return '✅';
      case 'SKIPPED': return '⏭';
      default: return '⏳';
    }
  };

  return (
    <div className="revision-page">
      <div className="revision-header">
        <div>
          <h1 className="page-title">Revision Checklist</h1>
          <p className="page-subtitle">
            AI-powered spaced repetition based on the Ebbinghaus forgetting curve
          </p>
        </div>
        <button className="generate-btn" onClick={generatePlan} disabled={loading}>
          {loading ? <div className="spinner" /> : <Sparkles size={16} />}
          {loading ? 'Generating...' : 'Generate AI Plan'}
        </button>
      </div>

      {status.text && (
        <p className={`status-message ${status.type}`}>
          {status.type === 'success' ? '✓ ' : status.type === 'error' ? '✗ ' : '⏳ '}
          {status.text}
        </p>
      )}

      {/* AI Plan Section */}
      <div className="section-title-row">
        <h2>AI Revision Plan</h2>
        {planItems.length > 0 && (
          <span className="count-badge">{planItems.length} notes</span>
        )}
      </div>

      {planItems.length === 0 ? (
        <div className="empty-state card" style={{ marginBottom: '2rem' }}>
          <div className="empty-state-icon">📋</div>
          <p>Click "Generate AI Plan" to analyze your notes and create a spaced-repetition schedule.</p>
        </div>
      ) : (
        <div className="plan-grid">
          {planItems.map((item, idx) => (
            <div key={idx} className="plan-card">
              <h3 className="plan-card-title">{item.title}</h3>

              <div className="badge-row">
                <span className={`complexity-badge complexity-${getComplexityClass(item.complexity)}`}>
                  Complexity: {item.complexity}/10
                </span>
                <span className={`urgency-badge urgency-${item.urgency}`}>
                  {item.urgency}
                </span>
              </div>

              <div className="complexity-bar-container">
                <div
                  className={`complexity-bar-fill ${getComplexityClass(item.complexity)}`}
                  style={{ width: `${item.complexity * 10}%` }}
                />
              </div>

              {item.reason && (
                <p className="plan-card-reason">{item.reason}</p>
              )}

              {item.suggestedDates && item.suggestedDates.length > 0 && (
                <div className="suggested-dates">
                  <div className="suggested-dates-header">Suggested Dates</div>
                  {item.suggestedDates.map((d, i) => (
                    <div key={i} className="suggested-date-item">• {formatDate(d)}</div>
                  ))}
                </div>
              )}

              <div className="schedule-row">
                <input
                  type="datetime-local"
                  value={scheduleDates[item.noteId] || ''}
                  onChange={(e) => setScheduleDates({ ...scheduleDates, [item.noteId]: e.target.value })}
                />
                <button className="schedule-btn" onClick={() => scheduleRevision(item)}>
                  Schedule
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Scheduled Revisions */}
      <div className="section-title-row">
        <h2>Scheduled Revisions</h2>
        {scheduledRevisions.length > 0 && (
          <span className="count-badge">{scheduledRevisions.length}</span>
        )}
      </div>

      {scheduledRevisions.length === 0 ? (
        <div className="empty-state card">
          <div className="empty-state-icon">🔔</div>
          <p>No revisions scheduled yet. Generate a plan and schedule your first review!</p>
        </div>
      ) : (
        <div>
          {scheduledRevisions.map((rev) => (
            <div key={rev.id} className="scheduled-row">
              <span className="scheduled-status-icon">{getStatusIcon(rev.status)}</span>
              <div className="scheduled-info">
                <div className="scheduled-note-title">
                  {rev.note?.title || 'Note'}
                </div>
                <div className="scheduled-meta">
                  Scheduled: {formatDate(rev.scheduledAt)} • {rev.status}
                  {rev.complexityScore && ` • Complexity: ${rev.complexityScore}/10`}
                </div>
              </div>
              {(rev.status === 'PENDING' || rev.status === 'SENT') && (
                <div className="scheduled-actions">
                  <button className="action-btn complete" onClick={() => updateStatus(rev.id, 'COMPLETED')}>
                    <CheckCircle size={13} /> Done
                  </button>
                  <button className="action-btn skip" onClick={() => updateStatus(rev.id, 'SKIPPED')}>
                    Skip
                  </button>
                  <button className="action-btn delete" onClick={() => deleteRevision(rev.id)}>
                    <Trash2 size={13} />
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default RevisionPage;
