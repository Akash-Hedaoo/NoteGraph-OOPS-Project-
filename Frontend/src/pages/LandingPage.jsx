import React, { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import * as THREE from 'three';
import './LandingPage.css';

/* ═══════════════════════════════════════════════════════════
   THREE.JS NEURAL NETWORK BACKGROUND
   Interactive particle system with glowing connections
   ═══════════════════════════════════════════════════════════ */
const NeuralBackground = () => {
  const mountRef = useRef(null);

  useEffect(() => {
    if (!mountRef.current) return;
    const container = mountRef.current;
    const w = window.innerWidth;
    const h = window.innerHeight;

    // Scene
    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(75, w / h, 0.1, 1000);
    camera.position.z = 300;

    const renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true });
    renderer.setSize(w, h);
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    container.appendChild(renderer.domElement);

    // Particles (nodes)
    const NODE_COUNT = 220;
    const positions = new Float32Array(NODE_COUNT * 3);
    const velocities = new Float32Array(NODE_COUNT * 3);
    const colors = new Float32Array(NODE_COUNT * 3);
    const sizes = new Float32Array(NODE_COUNT);

    const palette = [
      new THREE.Color('#0A66F0'), // blue
      new THREE.Color('#6366F1'), // indigo
      new THREE.Color('#EC4899'), // pink
      new THREE.Color('#10B981'), // emerald
      new THREE.Color('#F59E0B'), // amber
      new THREE.Color('#8B5CF6'), // violet
    ];

    for (let i = 0; i < NODE_COUNT; i++) {
      positions[i * 3] = (Math.random() - 0.5) * 600;
      positions[i * 3 + 1] = (Math.random() - 0.5) * 400;
      positions[i * 3 + 2] = (Math.random() - 0.5) * 300;

      velocities[i * 3] = (Math.random() - 0.5) * 0.3;
      velocities[i * 3 + 1] = (Math.random() - 0.5) * 0.3;
      velocities[i * 3 + 2] = (Math.random() - 0.5) * 0.15;

      const col = palette[Math.floor(Math.random() * palette.length)];
      colors[i * 3] = col.r;
      colors[i * 3 + 1] = col.g;
      colors[i * 3 + 2] = col.b;

      sizes[i] = Math.random() * 3 + 1.5;
    }

    const particleGeom = new THREE.BufferGeometry();
    particleGeom.setAttribute('position', new THREE.BufferAttribute(positions, 3));
    particleGeom.setAttribute('color', new THREE.BufferAttribute(colors, 3));
    particleGeom.setAttribute('size', new THREE.BufferAttribute(sizes, 1));

    // Custom shader for glowing particles
    const particleMat = new THREE.ShaderMaterial({
      uniforms: { time: { value: 0 } },
      vertexShader: `
        attribute float size;
        attribute vec3 color;
        varying vec3 vColor;
        varying float vAlpha;
        uniform float time;
        void main() {
          vColor = color;
          vec4 mvPosition = modelViewMatrix * vec4(position, 1.0);
          float pulse = 1.0 + 0.3 * sin(time * 2.0 + position.x * 0.01);
          gl_PointSize = size * pulse * (200.0 / -mvPosition.z);
          gl_Position = projectionMatrix * mvPosition;
          vAlpha = smoothstep(400.0, 100.0, -mvPosition.z);
        }
      `,
      fragmentShader: `
        varying vec3 vColor;
        varying float vAlpha;
        void main() {
          float d = length(gl_PointCoord - 0.5);
          if (d > 0.5) discard;
          float glow = 1.0 - smoothstep(0.0, 0.5, d);
          gl_FragColor = vec4(vColor, glow * vAlpha * 0.9);
        }
      `,
      transparent: true,
      depthWrite: false,
      blending: THREE.AdditiveBlending,
    });

    const particles = new THREE.Points(particleGeom, particleMat);
    scene.add(particles);

    // Connection lines
    const MAX_CONNECTIONS = 500;
    const linePositions = new Float32Array(MAX_CONNECTIONS * 6);
    const lineColors = new Float32Array(MAX_CONNECTIONS * 6);
    const lineGeom = new THREE.BufferGeometry();
    lineGeom.setAttribute('position', new THREE.BufferAttribute(linePositions, 3));
    lineGeom.setAttribute('color', new THREE.BufferAttribute(lineColors, 3));
    lineGeom.setDrawRange(0, 0);

    const lineMat = new THREE.LineBasicMaterial({
      vertexColors: true,
      transparent: true,
      opacity: 0.12,
      blending: THREE.AdditiveBlending,
      depthWrite: false,
    });
    const lines = new THREE.LineSegments(lineGeom, lineMat);
    scene.add(lines);

    // Mouse tracking
    const mouse = new THREE.Vector2(0, 0);
    const onMouseMove = (e) => {
      mouse.x = (e.clientX / w - 0.5) * 2;
      mouse.y = -(e.clientY / h - 0.5) * 2;
    };
    window.addEventListener('mousemove', onMouseMove);

    // Animation loop
    const clock = new THREE.Clock();
    let animId;

    const animate = () => {
      animId = requestAnimationFrame(animate);
      const t = clock.getElapsedTime();
      particleMat.uniforms.time.value = t;

      const pos = particleGeom.attributes.position.array;
      // Move particles
      for (let i = 0; i < NODE_COUNT; i++) {
        pos[i * 3] += velocities[i * 3];
        pos[i * 3 + 1] += velocities[i * 3 + 1];
        pos[i * 3 + 2] += velocities[i * 3 + 2];
        // Bounce at boundaries
        if (Math.abs(pos[i * 3]) > 300) velocities[i * 3] *= -1;
        if (Math.abs(pos[i * 3 + 1]) > 200) velocities[i * 3 + 1] *= -1;
        if (Math.abs(pos[i * 3 + 2]) > 150) velocities[i * 3 + 2] *= -1;
      }
      particleGeom.attributes.position.needsUpdate = true;

      // Update connections
      let lineIdx = 0;
      const connectionDist = 80;
      for (let i = 0; i < NODE_COUNT && lineIdx < MAX_CONNECTIONS; i++) {
        for (let j = i + 1; j < NODE_COUNT && lineIdx < MAX_CONNECTIONS; j++) {
          const dx = pos[i * 3] - pos[j * 3];
          const dy = pos[i * 3 + 1] - pos[j * 3 + 1];
          const dz = pos[i * 3 + 2] - pos[j * 3 + 2];
          const dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
          if (dist < connectionDist) {
            const li = lineIdx * 6;
            linePositions[li] = pos[i * 3];
            linePositions[li + 1] = pos[i * 3 + 1];
            linePositions[li + 2] = pos[i * 3 + 2];
            linePositions[li + 3] = pos[j * 3];
            linePositions[li + 4] = pos[j * 3 + 1];
            linePositions[li + 5] = pos[j * 3 + 2];
            const alpha = 1 - dist / connectionDist;
            lineColors[li] = colors[i * 3] * alpha;
            lineColors[li + 1] = colors[i * 3 + 1] * alpha;
            lineColors[li + 2] = colors[i * 3 + 2] * alpha;
            lineColors[li + 3] = colors[j * 3] * alpha;
            lineColors[li + 4] = colors[j * 3 + 1] * alpha;
            lineColors[li + 5] = colors[j * 3 + 2] * alpha;
            lineIdx++;
          }
        }
      }
      lineGeom.attributes.position.needsUpdate = true;
      lineGeom.attributes.color.needsUpdate = true;
      lineGeom.setDrawRange(0, lineIdx * 2);

      // Camera follows mouse smoothly
      camera.position.x += (mouse.x * 40 - camera.position.x) * 0.02;
      camera.position.y += (mouse.y * 25 - camera.position.y) * 0.02;
      camera.lookAt(0, 0, 0);

      // Slow global rotation
      particles.rotation.y = t * 0.03;
      lines.rotation.y = t * 0.03;

      renderer.render(scene, camera);
    };
    animate();

    // Resize handler
    const onResize = () => {
      const nw = window.innerWidth;
      const nh = window.innerHeight;
      camera.aspect = nw / nh;
      camera.updateProjectionMatrix();
      renderer.setSize(nw, nh);
    };
    window.addEventListener('resize', onResize);

    return () => {
      cancelAnimationFrame(animId);
      window.removeEventListener('mousemove', onMouseMove);
      window.removeEventListener('resize', onResize);
      if (container.contains(renderer.domElement)) {
        container.removeChild(renderer.domElement);
      }
      renderer.dispose();
    };
  }, []);

  return <div ref={mountRef} className="three-canvas-container" />;
};

/* ═══════════════════════════════════════════════════════════
   ANIMATED COUNTER
   ═══════════════════════════════════════════════════════════ */
const AnimatedCounter = ({ target, suffix = '' }) => {
  const [count, setCount] = useState(0);
  const ref = useRef(null);
  const started = useRef(false);

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting && !started.current) {
          started.current = true;
          const duration = 2000;
          const start = performance.now();
          const step = (now) => {
            const progress = Math.min((now - start) / duration, 1);
            const eased = 1 - Math.pow(1 - progress, 3);
            setCount(Math.floor(eased * target));
            if (progress < 1) requestAnimationFrame(step);
          };
          requestAnimationFrame(step);
        }
      },
      { threshold: 0.5 }
    );
    if (ref.current) observer.observe(ref.current);
    return () => observer.disconnect();
  }, [target]);

  return <span ref={ref}>{count.toLocaleString()}{suffix}</span>;
};

/* ═══════════════════════════════════════════════════════════
   SCROLL REVEAL HOOK
   ═══════════════════════════════════════════════════════════ */
const useReveal = () => {
  useEffect(() => {
    const elements = document.querySelectorAll('.reveal');
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('visible');
          }
        });
      },
      { threshold: 0.15 }
    );
    elements.forEach((el) => observer.observe(el));
    return () => observer.disconnect();
  }, []);
};

/* ═══════════════════════════════════════════════════════════
   LANDING PAGE COMPONENT
   ═══════════════════════════════════════════════════════════ */
const LandingPage = () => {
  useReveal();

  const features = [
    { icon: '🧠', title: 'Knowledge Graph', desc: 'Visualize the invisible connections between your ideas with an interactive force-directed neural graph.', color: 'purple' },
    { icon: '✨', title: 'AI Assistant', desc: 'Chat with AI about your notes, get smart tag suggestions, and auto-format content with one click.', color: 'blue' },
    { icon: '📝', title: 'Rich Editor', desc: 'Write beautifully with rich text formatting, real-time auto-save, and keyboard shortcuts.', color: 'green' },
    { icon: '🏷️', title: 'Smart Tags', desc: 'Organize notes with color-coded tags that create connections across your entire knowledge base.', color: 'amber' },
    { icon: '⭐', title: 'Favorites', desc: 'Star your most important notes for instant access. Search and filter your starred collection.', color: 'pink' },
    { icon: '📊', title: 'Org Chart View', desc: 'See your note hierarchy in a beautiful org-chart layout with tag-based branches and leaf nodes.', color: 'cyan' },
  ];

  return (
    <div className="landing-page">
      <NeuralBackground />

      {/* Navigation */}
      <nav className="landing-nav">
        <div className="nav-brand">
          <div className="nav-logo">◆</div>
          <span className="nav-title">NoteGraph</span>
        </div>
        <div className="nav-links">
          <a href="#features" className="nav-link">Features</a>
          <a href="#demo" className="nav-link">How It Works</a>
          <a href="#stats" className="nav-link">Stats</a>
          <Link to="/login" className="nav-cta">Get Started →</Link>
        </div>
      </nav>

      {/* Hero */}
      <section className="hero-section">
        <div className="hero-floating-cards">
          <div className="floating-card">
            <div className="floating-card-line" style={{ width: '70%', background: 'rgba(99,102,241,0.3)' }} />
            <div className="floating-card-line" style={{ width: '100%', background: 'rgba(255,255,255,0.05)' }} />
            <div className="floating-card-line" style={{ width: '85%', background: 'rgba(255,255,255,0.04)' }} />
            <div className="floating-card-line" style={{ width: '60%', background: 'rgba(255,255,255,0.03)' }} />
          </div>
          <div className="floating-card">
            <div className="floating-card-line" style={{ width: '90%', background: 'rgba(236,72,153,0.25)' }} />
            <div className="floating-card-line" style={{ width: '65%', background: 'rgba(255,255,255,0.05)' }} />
            <div className="floating-card-line" style={{ width: '80%', background: 'rgba(255,255,255,0.03)' }} />
          </div>
          <div className="floating-card">
            <div className="floating-card-line" style={{ width: '100%', background: 'rgba(16,185,129,0.3)' }} />
            <div className="floating-card-line" style={{ width: '75%', background: 'rgba(255,255,255,0.04)' }} />
          </div>
          <div className="floating-card">
            <div className="floating-card-line" style={{ width: '60%', background: 'rgba(245,158,11,0.3)' }} />
            <div className="floating-card-line" style={{ width: '95%', background: 'rgba(255,255,255,0.05)' }} />
            <div className="floating-card-line" style={{ width: '45%', background: 'rgba(255,255,255,0.03)' }} />
          </div>
        </div>

        <div className="hero-badge">
          <span className="pulse-dot" />
          Powered by AI • Built for Thinkers
        </div>

        <h1 className="hero-title">
          Your Second Brain,<br/>
          <span className="gradient-text">Beautifully Connected</span>
        </h1>

        <p className="hero-subtitle">
          NoteGraph transforms scattered thoughts into an interconnected knowledge network.
          Write, organize, and discover insights with AI-powered intelligence.
        </p>

        <div className="hero-actions">
          <Link to="/login" className="hero-btn-primary">
            Start Building Your Graph →
          </Link>
          <a href="#features" className="hero-btn-secondary">
            See How It Works
          </a>
        </div>
      </section>

      {/* Stats */}
      <section id="stats" className="stats-section">
        <div className="stats-grid reveal">
          <div className="stat-item">
            <div className="stat-number"><AnimatedCounter target={10000} suffix="+" /></div>
            <div className="stat-label">Notes Created</div>
          </div>
          <div className="stat-item">
            <div className="stat-number"><AnimatedCounter target={50000} suffix="+" /></div>
            <div className="stat-label">Connections Made</div>
          </div>
          <div className="stat-item">
            <div className="stat-number"><AnimatedCounter target={99} suffix="%" /></div>
            <div className="stat-label">Uptime</div>
          </div>
          <div className="stat-item">
            <div className="stat-number"><AnimatedCounter target={4.9} /></div>
            <div className="stat-label">User Rating</div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section id="features" className="features-section">
        <div className="section-header reveal">
          <div className="section-label">Features</div>
          <h2 className="section-title">Everything You Need to Think Better</h2>
          <p className="section-subtitle">
            From rich editing to AI insights — every tool designed to amplify your thinking.
          </p>
        </div>

        <div className="features-grid">
          {features.map((f, i) => (
            <div key={i} className={`feature-card reveal reveal-delay-${i % 6 + 1}`}>
              <div className={`feature-icon ${f.color}`}>{f.icon}</div>
              <h3 className="feature-title">{f.title}</h3>
              <p className="feature-desc">{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Demo / How It Works */}
      <section id="demo" className="demo-section">
        <div className="section-header reveal">
          <div className="section-label">How It Works</div>
          <h2 className="section-title">Three Steps to Brilliance</h2>
        </div>

        <div className="demo-container">
          <div className="demo-content reveal">
            <div className="demo-feature-item">
              <div className="demo-feature-num">1</div>
              <div className="demo-feature-text">
                <h4>Create & Write</h4>
                <p>Start writing notes in our rich editor with auto-save, formatting tools, and AI assistance.</p>
              </div>
            </div>
            <div className="demo-feature-item">
              <div className="demo-feature-num">2</div>
              <div className="demo-feature-text">
                <h4>Tag & Connect</h4>
                <p>Add tags to your notes — AI can even suggest them. Each tag creates neural connections.</p>
              </div>
            </div>
            <div className="demo-feature-item">
              <div className="demo-feature-num">3</div>
              <div className="demo-feature-text">
                <h4>Explore & Discover</h4>
                <p>Watch your knowledge graph grow. Discover hidden connections between your ideas.</p>
              </div>
            </div>
          </div>

          <div className="demo-mockup reveal reveal-delay-2">
            <div className="mockup-window">
              <div className="mockup-titlebar">
                <div className="mockup-dot red" />
                <div className="mockup-dot yellow" />
                <div className="mockup-dot green" />
              </div>
              <div className="mockup-body">
                <div className="mockup-sidebar">
                  <div className="mockup-sidebar-item active">◆ Dashboard</div>
                  <div className="mockup-sidebar-item">📊 Note Graph</div>
                  <div className="mockup-sidebar-item">🧠 Knowledge</div>
                  <div className="mockup-sidebar-item">🏷️ Tags</div>
                  <div className="mockup-sidebar-item">⭐ Favorites</div>
                </div>
                <div className="mockup-editor">
                  <div className="mockup-editor-line" style={{ width: '60%', height: 14, background: 'rgba(99,102,241,0.4)' }} />
                  <div className="mockup-editor-line" style={{ width: '100%', background: 'rgba(255,255,255,0.06)' }} />
                  <div className="mockup-editor-line" style={{ width: '90%', background: 'rgba(255,255,255,0.05)' }} />
                  <div className="mockup-editor-line" style={{ width: '75%', background: 'rgba(255,255,255,0.04)' }} />
                  <div className="mockup-editor-line" style={{ width: '95%', background: 'rgba(255,255,255,0.06)' }} />
                  <div className="mockup-editor-line" style={{ width: '50%', background: 'rgba(255,255,255,0.03)' }} />
                  <div style={{ display: 'flex', gap: 6, marginTop: 12 }}>
                    <div style={{ padding: '4px 10px', borderRadius: 8, background: 'rgba(99,102,241,0.2)', fontSize: 11, color: '#a5b4fc' }}>#research</div>
                    <div style={{ padding: '4px 10px', borderRadius: 8, background: 'rgba(16,185,129,0.2)', fontSize: 11, color: '#6ee7b7' }}>#ideas</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="cta-section">
        <div className="cta-glow" />
        <h2 className="cta-title reveal">
          Ready to Build Your<br/>
          <span className="gradient-text" style={{ backgroundSize: '200% 200%' }}>Knowledge Graph?</span>
        </h2>
        <p className="cta-subtitle reveal reveal-delay-1">
          Join thousands of thinkers who use NoteGraph to connect their ideas and think more clearly.
        </p>
        <Link to="/login" className="cta-btn reveal reveal-delay-2">
          ✨ Get Started — It's Free
        </Link>
      </section>

      {/* Footer */}
      <footer className="landing-footer">
        <div className="footer-brand">
          <span style={{ color: '#6366F1', fontSize: 18 }}>◆</span>
          NoteGraph
        </div>
        <div className="footer-links">
          <span className="footer-link">Features</span>
          <span className="footer-link">Docs</span>
          <span className="footer-link">GitHub</span>
          <span className="footer-link">© 2026</span>
        </div>
      </footer>
    </div>
  );
};

export default LandingPage;
