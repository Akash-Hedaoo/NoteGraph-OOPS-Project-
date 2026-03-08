import React, { useState, useEffect } from 'react';
import { 
  ChevronRight, Filter, ArrowUpDown, FileText, Share2, 
  Settings, Maximize, X, Activity, User, Tag
} from 'lucide-react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
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
  const { workspaceId, user } = useAuth();
  const [graphData, setGraphData] = useState([]);
  const [selectedTag, setSelectedTag] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchGraph = async () => {
      if (!workspaceId) return;
      try {
        const response = await api.get(`/graph/workspace/${workspaceId}`);
        // The endpoint returns a GraphDto with 'nodes' array
        setGraphData(response.data.nodes || []);
        if (response.data.nodes && response.data.nodes.length > 0) {
          setSelectedTag(response.data.nodes[0]);
        }
      } catch (err) {
        console.error("Failed to fetch graph data", err);
      } finally {
        setLoading(false);
      }
    };

    fetchGraph();
  }, [workspaceId]);

  return (
    <div className="note-hierarchy-page">
      <div className="hierarchy-main-area">
        {/* Header Breadcrumbs */}
        <header className="page-header-row">
          <div className="breadcrumbs">
            <span>Workspaces</span>
            <ChevronRight size={14} className="text-secondary" />
            <span className="current-path">Global Tag Graph</span>
          </div>
          
          <div className="header-actions-flex">
            <h1 className="page-title">Note Connections</h1>
            <div className="toolbar-actions">
               <div className="view-toggles card">
                 <button className="toggle-btn active" title="Org Chart View">
                   <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="8" y="3" width="8" height="6" rx="1"/><rect x="2" y="15" width="8" height="6" rx="1"/><rect x="14" y="15" width="8" height="6" rx="1"/><path d="M12 9v2"/><path d="M6 15v-2a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v2"/></svg>
                 </button>
                 <button className="toggle-btn" title="Force Graph View">
                   <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/></svg>
                 </button>
               </div>
               <button className="btn-outline"><Filter size={16} /> Filter by Tag</button>
               <button className="icon-btn-small border-btn"><Maximize size={16} /></button>
            </div>
          </div>
        </header>

        {/* Organization Chart Canvas */}
        <div className="hierarchy-canvas card org-chart-canvas">

          <div className="org-tree">
            {/* Root Note Level */}
            <div className="org-level">
              <GraphNode title={`${user?.name || 'My'} Workspace Core`} isRoot={true} tagColor="blue" />
            </div>

            {/* Connecting Line Down from Root */}
            {graphData.length > 0 && <div className="org-line-down"></div>}

            {/* Horizontal Connecting Line for Branches */}
            {graphData.length > 1 && <div className="org-line-horizontal" style={{ width: `${Math.min(graphData.length * 200, 800)}px` }}></div>}

            {/* Tag Categories Level */}
            <div className="org-level branches">
              
              {loading ? (
                <div style={{ padding: '20px', color: 'var(--text-secondary)' }}>Loading graph...</div>
              ) : graphData.length === 0 ? (
                <div style={{ padding: '20px', color: 'var(--text-secondary)' }}>No tags created yet. Create some notes and add tags to see the connections!</div>
              ) : (
                graphData.map((tagNode, index) => {
                  const color = COLORS[index % COLORS.length];
                  return (
                    <div key={tagNode.tagId} className="org-branch">
                      <div className="org-line-down short"></div>
                      <GraphNode 
                        type="tag" 
                        title={tagNode.tagName} 
                        tagColor={color} 
                        count={tagNode.noteCount} 
                        onClick={() => setSelectedTag({...tagNode, color})}
                      />
                      <div className="org-line-down short"></div>
                      
                      <div className="org-leaves">
                        {tagNode.notes.map((note, noteIdx) => (
                          <React.Fragment key={note.id}>
                            <GraphNode title={note.title || 'Untitled Note'} tagColor={color} />
                            {noteIdx < tagNode.notes.length - 1 && <div className="leaf-connector"></div>}
                          </React.Fragment>
                        ))}
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </div>

        </div>
      </div>

      {/* Right Sidebar - Connection Details */}
      {selectedTag && (
        <aside className="note-details-sidebar">
          <div className="details-header">
            <h3>Connection Details</h3>
            <button className="icon-btn-small" onClick={() => setSelectedTag(null)}><X size={18} /></button>
          </div>

          <div className={`preview-card bg-purple-tint`}>
            <div className={`tag-badge-large bg-${selectedTag.color || 'purple'}`}>
              <Tag size={20} color="white" />
            </div>
            <h4 className={`mt-4 text-${selectedTag.color || 'purple'}-dark`}>#{selectedTag.tagName}</h4>
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
              {selectedTag.notes.map(note => (
                <div key={note.id} className="mini-note-result card">
                  <div className={`icon-box bg-${selectedTag.color || 'purple'}-light text-${selectedTag.color || 'purple'}`}>
                    <FileText size={16} />
                  </div>
                  <div className="c-info">
                     <span className="c-title">{note.title || 'Untitled Note'}</span>
                     <span className="c-meta">Updated: {new Date(note.updatedAt).toLocaleDateString()}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </aside>
      )}
    </div>
  );
};

export default NoteHierarchy;
