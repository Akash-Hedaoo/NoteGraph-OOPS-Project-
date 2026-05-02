import React, { useEffect, useRef, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import './KnowledgeGraph.css';

/* ═══════════════════════════════════════════════════════════
   KNOWLEDGE GRAPH — Obsidian-style force-directed graph
   Matching Java UI's KnowledgeGraphPanel physics & rendering.
   ═══════════════════════════════════════════════════════════ */

const TAG_COLORS = [
  '#4F46E5', '#059669', '#D97706', '#DC2626',
  '#7C3AED', '#2563EB', '#DB2777', '#0D9488',
  '#E11D48', '#0EA5E9', '#16A34A', '#EA580C',
];

const REPULSION = 8000;
const ATTRACTION = 0.005;
const SPRING_LENGTH = 180;
const GRAVITY = 0.02;
const DAMPING = 0.85;
const NOTE_RADIUS = 18;
const TAG_RADIUS = 28;

const KnowledgeGraph = () => {
  const navigate = useNavigate();
  const { workspaceId } = useAuth();
  const canvasRef = useRef(null);
  const wrapperRef = useRef(null);
  const nodesRef = useRef([]);
  const edgesRef = useRef([]);
  const animRef = useRef(null);
  const hoveredRef = useRef(null);
  const draggedRef = useRef(null);
  const panRef = useRef({ isPanning: false, startX: 0, startY: 0 });
  const cameraRef = useRef({ offsetX: 0, offsetY: 0, zoom: 1 });
  const tooltipRef = useRef(null);
  const [stats, setStats] = useState({ notes: 0, tags: 0, edges: 0 });
  const [zoom, setZoom] = useState(100);
  const [isEmpty, setIsEmpty] = useState(true);

  // ── Load Data ──────────────────────────────────
  useEffect(() => {
    if (!workspaceId) return;
    const load = async () => {
      try {
        const [notesRes, tagsRes] = await Promise.all([
          api.get(`/notes/workspace/${workspaceId}`),
          api.get(`/tags/workspace/${workspaceId}`),
        ]);
        buildGraph(notesRes.data, tagsRes.data);
      } catch (e) {
        console.error('Failed to load graph data', e);
      }
    };
    load();
  }, [workspaceId]);

  const buildGraph = (notesList, tagsList) => {
    const nodes = [];
    const edges = [];

    // Tag hubs
    tagsList.forEach((tag, i) => {
      const color = TAG_COLORS[i % TAG_COLORS.length];
      nodes.push({
        id: `tag:${tag.id}`, label: `#${tag.name}`, isTag: true, color,
        x: (Math.random() - 0.5) * 600, y: (Math.random() - 0.5) * 400,
        vx: 0, vy: 0, radius: TAG_RADIUS,
      });
    });

    // Note nodes
    notesList.forEach((note) => {
      let noteColor = '#64748b';
      if (note.tags?.length > 0) {
        const tagNode = nodes.find(n => n.id === `tag:${note.tags[0].id}`);
        if (tagNode) noteColor = tagNode.color;
      }
      let label = note.title || 'Untitled';
      if (label.length > 20) label = label.substring(0, 18) + '…';
      nodes.push({
        id: `note:${note.id}`, label, isTag: false, color: noteColor,
        x: (Math.random() - 0.5) * 600, y: (Math.random() - 0.5) * 400,
        vx: 0, vy: 0, radius: NOTE_RADIUS,
      });
    });

    // Edges
    notesList.forEach((note) => {
      if (!note.tags) return;
      const noteNode = nodes.find(n => n.id === `note:${note.id}`);
      if (!noteNode) return;
      note.tags.forEach((tag) => {
        const tagNode = nodes.find(n => n.id === `tag:${tag.id}`);
        if (tagNode) edges.push({ from: noteNode, to: tagNode, color: tagNode.color });
      });
    });

    // Untagged hub
    const untagged = notesList.filter(n => !n.tags || n.tags.length === 0);
    if (untagged.length > 0) {
      const hub = {
        id: 'tag:__untagged__', label: 'Untagged', isTag: true, color: '#94a3b8',
        x: (Math.random() - 0.5) * 600, y: (Math.random() - 0.5) * 400,
        vx: 0, vy: 0, radius: TAG_RADIUS,
      };
      nodes.push(hub);
      untagged.forEach((note) => {
        const nn = nodes.find(n => n.id === `note:${note.id}`);
        if (nn) edges.push({ from: nn, to: hub, color: hub.color });
      });
    }

    nodesRef.current = nodes;
    edgesRef.current = edges;
    setStats({
      notes: nodes.filter(n => !n.isTag).length,
      tags: nodes.filter(n => n.isTag).length,
      edges: edges.length,
    });
    setIsEmpty(nodes.length === 0);
  };

  // ── Physics Step ───────────────────────────────
  const stepPhysics = useCallback(() => {
    const nodes = nodesRef.current;
    const edges = edgesRef.current;

    for (let i = 0; i < nodes.length; i++) {
      for (let j = i + 1; j < nodes.length; j++) {
        const a = nodes[i], b = nodes[j];
        let dx = b.x - a.x, dy = b.y - a.y;
        let dist = Math.sqrt(dx * dx + dy * dy) || 1;
        const force = REPULSION / (dist * dist);
        const fx = (dx / dist) * force, fy = (dy / dist) * force;
        a.vx -= fx; a.vy -= fy;
        b.vx += fx; b.vy += fy;
      }
    }

    edges.forEach(({ from, to }) => {
      let dx = to.x - from.x, dy = to.y - from.y;
      let dist = Math.sqrt(dx * dx + dy * dy) || 1;
      const force = (dist - SPRING_LENGTH) * ATTRACTION;
      const fx = (dx / dist) * force, fy = (dy / dist) * force;
      from.vx += fx; from.vy += fy;
      to.vx -= fx; to.vy -= fy;
    });

    nodes.forEach((n) => { n.vx -= n.x * GRAVITY; n.vy -= n.y * GRAVITY; });

    nodes.forEach((n) => {
      if (n === draggedRef.current) return;
      n.vx *= DAMPING; n.vy *= DAMPING;
      n.x += n.vx; n.y += n.vy;
    });
  }, []);

  // ── Canvas Rendering ──────────────────────────
  const draw = useCallback(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const w = canvas.width, h = canvas.height;
    const { offsetX, offsetY, zoom } = cameraRef.current;

    ctx.clearRect(0, 0, w, h);
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    ctx.fillStyle = isDark ? '#0F172A' : '#f8fafc';
    ctx.fillRect(0, 0, w, h);

    // Dot grid
    ctx.fillStyle = isDark ? 'rgba(59, 130, 246, 0.08)' : 'rgba(10, 102, 240, 0.06)';
    for (let x = 0; x < w; x += 24) {
      for (let y = 0; y < h; y += 24) {
        ctx.beginPath();
        ctx.arc(x, y, 1, 0, Math.PI * 2);
        ctx.fill();
      }
    }

    const nodes = nodesRef.current;
    const edges = edgesRef.current;
    const hovered = hoveredRef.current;

    const toScreenX = (wx) => wx * zoom + w / 2 + offsetX;
    const toScreenY = (wy) => wy * zoom + h / 2 + offsetY;

    // Connected IDs
    const connectedIds = new Set();
    if (hovered) {
      connectedIds.add(hovered.id);
      edges.forEach(({ from, to }) => {
        if (from === hovered) connectedIds.add(to.id);
        if (to === hovered) connectedIds.add(from.id);
      });
    }

    // Draw edges
    edges.forEach(({ from, to, color }) => {
      const x1 = toScreenX(from.x), y1 = toScreenY(from.y);
      const x2 = toScreenX(to.x), y2 = toScreenY(to.y);
      const highlighted = hovered && (from === hovered || to === hovered);
      const dimmed = hovered && !highlighted;

      ctx.beginPath();
      ctx.moveTo(x1, y1); ctx.lineTo(x2, y2);
      ctx.strokeStyle = dimmed
        ? `${color}1E`
        : highlighted ? `${color}C8` : `${color}50`;
      ctx.lineWidth = (highlighted ? 2.5 : 1.5) * zoom;
      ctx.stroke();
    });

    // Draw nodes
    nodes.forEach((node) => {
      const sx = toScreenX(node.x), sy = toScreenY(node.y);
      const r = node.radius * zoom;
      const isHovered = node === hovered;
      const isConnected = connectedIds.has(node.id);
      const isDimmed = hovered && !isConnected;

      if (isDimmed) {
        ctx.globalAlpha = 0.25;
      }

      // Glow
      if (isHovered || isConnected) {
        ctx.beginPath();
        const glowR = r * (isHovered ? 2.2 : 1.6);
        ctx.arc(sx, sy, glowR, 0, Math.PI * 2);
        ctx.fillStyle = `${node.color}${isHovered ? '33' : '19'}`;
        ctx.fill();
      }

      if (node.isTag) {
        ctx.beginPath();
        ctx.arc(sx, sy, r, 0, Math.PI * 2);
        ctx.fillStyle = node.color;
        ctx.fill();
        ctx.strokeStyle = '#fff';
        ctx.lineWidth = 2 * zoom;
        ctx.stroke();
        // Label inside
        ctx.fillStyle = '#fff';
        ctx.font = `bold ${Math.max(10, 13 * zoom)}px Inter, sans-serif`;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        let lbl = node.label;
        if (ctx.measureText(lbl).width > r * 2 - 4) lbl = lbl.substring(0, 4) + '…';
        ctx.fillText(lbl, sx, sy);
      } else {
        ctx.beginPath();
        ctx.arc(sx, sy, r, 0, Math.PI * 2);
        ctx.fillStyle = isDark ? '#1E293B' : '#fff';
        ctx.fill();
        ctx.strokeStyle = node.color;
        ctx.lineWidth = 2 * zoom;
        ctx.stroke();
        // Inner dot
        ctx.beginPath();
        ctx.arc(sx, sy, 3 * zoom, 0, Math.PI * 2);
        ctx.fillStyle = node.color;
        ctx.fill();
      }

      // Label below
      if (zoom > 0.4) {
        ctx.fillStyle = isHovered ? (isDark ? '#F1F5F9' : '#111827') : (isDark ? '#94A3B8' : '#6B7280');
        ctx.font = `${isHovered ? 'bold ' : ''}${Math.max(9, 11 * zoom)}px Inter, sans-serif`;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'top';
        // Background behind label
        const lbl = node.label;
        const tw = ctx.measureText(lbl).width;
        ctx.fillStyle = isDark ? 'rgba(15, 23, 42, 0.85)' : 'rgba(248, 250, 252, 0.85)';
        ctx.fillRect(sx - tw / 2 - 2, sy + r + 4, tw + 4, 16);
        ctx.fillStyle = isHovered ? (isDark ? '#F1F5F9' : '#111827') : (isDark ? '#94A3B8' : '#6B7280');
        ctx.fillText(lbl, sx, sy + r + 6);
      }

      ctx.globalAlpha = 1;
    });
  }, []);

  // ── Animation Loop ────────────────────────────
  useEffect(() => {
    // Only start loop when we have data (canvas is in the DOM)
    if (isEmpty) return;

    const canvas = canvasRef.current;
    const wrapper = wrapperRef.current;
    if (!canvas || !wrapper) return;

    const resize = () => {
      const rect = wrapper.getBoundingClientRect();
      if (rect.width > 0 && rect.height > 0) {
        canvas.width = rect.width;
        canvas.height = rect.height;
      }
    };

    // Use ResizeObserver for reliable sizing
    const resizeObserver = new ResizeObserver(() => resize());
    resizeObserver.observe(wrapper);

    // Initial resize with a small delay to let layout settle
    requestAnimationFrame(() => {
      resize();
    });

    const loop = () => {
      stepPhysics();
      draw();
      animRef.current = requestAnimationFrame(loop);
    };
    animRef.current = requestAnimationFrame(loop);

    return () => {
      cancelAnimationFrame(animRef.current);
      resizeObserver.disconnect();
    };
  }, [isEmpty, stepPhysics, draw]);

  // ── Interaction Handlers ──────────────────────
  const screenToWorld = (sx, sy) => {
    const canvas = canvasRef.current;
    if (!canvas) return { x: 0, y: 0 };
    const { offsetX, offsetY, zoom } = cameraRef.current;
    return {
      x: (sx - offsetX - canvas.width / 2) / zoom,
      y: (sy - offsetY - canvas.height / 2) / zoom,
    };
  };

  const hitTest = (sx, sy) => {
    const { x: wx, y: wy } = screenToWorld(sx, sy);
    for (const n of nodesRef.current) {
      const dx = n.x - wx, dy = n.y - wy;
      if (Math.sqrt(dx * dx + dy * dy) <= n.radius + 4) return n;
    }
    return null;
  };

  const handleMouseDown = (e) => {
    const rect = canvasRef.current.getBoundingClientRect();
    const mx = e.clientX - rect.left, my = e.clientY - rect.top;
    const hit = hitTest(mx, my);
    if (hit) {
      draggedRef.current = hit;
    } else {
      panRef.current = { isPanning: true, startX: e.clientX, startY: e.clientY };
    }
  };

  const handleMouseMove = (e) => {
    const rect = canvasRef.current.getBoundingClientRect();
    const mx = e.clientX - rect.left, my = e.clientY - rect.top;

    if (draggedRef.current) {
      const { x, y } = screenToWorld(mx, my);
      draggedRef.current.x = x;
      draggedRef.current.y = y;
      draggedRef.current.vx = 0;
      draggedRef.current.vy = 0;
    } else if (panRef.current.isPanning) {
      cameraRef.current.offsetX += e.clientX - panRef.current.startX;
      cameraRef.current.offsetY += e.clientY - panRef.current.startY;
      panRef.current.startX = e.clientX;
      panRef.current.startY = e.clientY;
    } else {
      const hit = hitTest(mx, my);
      hoveredRef.current = hit;
      canvasRef.current.style.cursor = hit ? 'pointer' : 'crosshair';

      // Tooltip
      if (tooltipRef.current) {
        if (hit) {
          tooltipRef.current.style.display = 'block';
          tooltipRef.current.style.left = `${mx}px`;
          tooltipRef.current.style.top = `${my - 12}px`;
          tooltipRef.current.textContent = hit.isTag ? `Tag: ${hit.label}` : `${hit.label} (click to open)`;
        } else {
          tooltipRef.current.style.display = 'none';
        }
      }
    }
  };

  const handleMouseUp = () => {
    draggedRef.current = null;
    panRef.current.isPanning = false;
  };

  const handleClick = (e) => {
    const rect = canvasRef.current.getBoundingClientRect();
    const mx = e.clientX - rect.left, my = e.clientY - rect.top;
    const hit = hitTest(mx, my);
    if (hit && !hit.isTag) {
      const noteId = hit.id.replace('note:', '');
      navigate(`/editor/${noteId}`);
    }
  };

  const handleWheel = (e) => {
    e.preventDefault();
    const scaleFactor = e.deltaY < 0 ? 1.1 : 0.9;
    const newZoom = cameraRef.current.zoom * scaleFactor;
    if (newZoom < 0.2 || newZoom > 5) return;

    const rect = canvasRef.current.getBoundingClientRect();
    const mx = e.clientX - rect.left, my = e.clientY - rect.top;
    cameraRef.current.offsetX = mx - (mx - cameraRef.current.offsetX) * (newZoom / cameraRef.current.zoom);
    cameraRef.current.offsetY = my - (my - cameraRef.current.offsetY) * (newZoom / cameraRef.current.zoom);
    cameraRef.current.zoom = newZoom;
    setZoom(Math.round(newZoom * 100));
  };

  return (
    <div className="kg-page">
      <div className="kg-header">
        <h1 className="kg-title">Knowledge Graph</h1>
        <p className="kg-subtitle">Visualize how your notes connect — drag, zoom, and explore your second brain.</p>
      </div>

      <div className="kg-canvas-wrapper" ref={wrapperRef}>
        {isEmpty ? (
          <div className="kg-empty">
            <div className="kg-empty-icon">🧠</div>
            <div className="kg-empty-title">Your Knowledge Graph is Empty</div>
            <div className="kg-empty-desc">Create notes and add tags to see your neural knowledge network come alive!</div>
          </div>
        ) : (
          <>
            <canvas
              ref={canvasRef}
              onMouseDown={handleMouseDown}
              onMouseMove={handleMouseMove}
              onMouseUp={handleMouseUp}
              onMouseLeave={handleMouseUp}
              onClick={handleClick}
              onWheel={handleWheel}
            />
            <div ref={tooltipRef} className="kg-tooltip" style={{ display: 'none' }} />
            <div className="kg-legend">
              <div className="kg-legend-title">Legend</div>
              <div className="kg-legend-item">
                <div className="kg-legend-dot tag-hub" />
                <span>Tag Hub</span>
              </div>
              <div className="kg-legend-item">
                <div className="kg-legend-dot note-node" />
                <span>Note</span>
              </div>
            </div>
            <div className="kg-stats">
              <div className="kg-stats-title">Graph Stats</div>
              <div className="kg-stats-row">📄 {stats.notes} Notes</div>
              <div className="kg-stats-row">🏷️ {stats.tags} Tags</div>
              <div className="kg-stats-row">🔗 {stats.edges} Connections</div>
            </div>
            <div className="kg-zoom">{zoom}%</div>
          </>
        )}
      </div>
    </div>
  );
};

export default KnowledgeGraph;
