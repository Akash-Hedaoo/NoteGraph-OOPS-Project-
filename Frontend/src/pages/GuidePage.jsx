import React, { useState } from 'react';
import { 
  BookOpen, ChevronRight, ChevronDown, FileText, FolderClosed, Tag, Star, 
  Plus, Edit2, Trash2, Search, Share2, Sparkles, Layers, Zap, ArrowRight
} from 'lucide-react';
import './GuidePage.css';

const guideSteps = [
  {
    id: 'workspaces',
    icon: FolderClosed,
    color: '#3B82F6',
    title: 'Workspaces',
    subtitle: 'Organize your projects',
    content: [
      { heading: 'What are Workspaces?', text: 'Workspaces are the top-level containers for your notes. Think of them as separate projects or areas of focus — like "React Project", "College Notes", or "Personal Ideas".' },
      { heading: 'Create a Workspace', text: 'Click the workspace dropdown in the sidebar, then click "Create new workspace". Give it a meaningful name and hit Create.' },
      { heading: 'Switch Workspaces', text: 'Use the workspace dropdown in the sidebar to switch between workspaces. All your notes, tags, and connections will update to show the selected workspace.' },
      { heading: 'Rename a Workspace', text: 'Click the pencil icon (✏️) next to a workspace in the dropdown menu, or use the edit icon on the Note Graph page.' },
      { heading: 'Delete a Workspace', text: 'Click the trash icon next to a workspace. Warning: this permanently deletes all notes and tags inside it.' },
    ]
  },
  {
    id: 'notes',
    icon: FileText,
    color: '#10B981',
    title: 'Creating & Editing Notes',
    subtitle: 'Write beautiful notes',
    content: [
      { heading: 'Create a New Note', text: 'Click the "+ New Note" button in the sidebar. This opens the Editor with a blank note. Start typing your title and content right away.' },
      { heading: 'Rich Text Editor', text: 'Use the formatting toolbar to style your notes — Bold, Italic, Underline, Headings (H1, H2), Bullet Lists, Numbered Lists, Checklists, Links, and Images.' },
      { heading: 'Choose Workspace & Section', text: 'When editing a note, use the blue Workspace selector and purple Section selector below the title to assign your note to a specific workspace and tag section.' },
      { heading: 'Auto-Save', text: 'Your notes are automatically saved as you type. Look for the "Saved" indicator in the top bar to confirm your changes are persisted.' },
      { heading: 'Delete a Note', text: 'Click the trash icon in the editor toolbar. A confirmation dialog will appear before the note is permanently deleted.' },
    ]
  },
  {
    id: 'tags',
    icon: Tag,
    color: '#8B5CF6',
    title: 'Tags & Sections',
    subtitle: 'Categorize your notes',
    content: [
      { heading: 'What are Tags?', text: 'Tags (also called "Sections") are labels you add to notes. They create the connections you see in the Note Graph — notes with the same tag are grouped together.' },
      { heading: 'Create a Tag', text: 'Go to the Tags page from the sidebar, then click "+ Add New Tag". Enter a name and choose a color for your tag.' },
      { heading: 'Add Tags to Notes', text: 'In the Editor, use the purple Section selector or click "+ Add tag" to attach tags to your note. You can also use the right sidebar of the Editor.' },
      { heading: 'Rename a Section', text: 'On the Note Graph page (tree view), click the pencil icon next to any section header to rename it.' },
      { heading: 'Delete a Tag', text: 'Use the trash icon on the Tags page or the Note Graph page. Notes will be unlinked from the tag but not deleted.' },
    ]
  },
  {
    id: 'graph',
    icon: Share2,
    color: '#F59E0B',
    title: 'Note Graph & Connections',
    subtitle: 'Visualize your knowledge',
    content: [
      { heading: 'The Note Graph', text: 'Navigate to "Note Graph" in the sidebar to see how your notes are connected. This page shows the relationship between your workspace, tags, and notes.' },
      { heading: 'Org Chart View', text: 'The default view shows an organizational chart with your workspace at the top, tags as branches, and individual notes as leaves.' },
      { heading: 'Tree List View', text: 'Switch to tree view for a collapsible list layout. Click on any section to expand/collapse it and see the notes within.' },
      { heading: 'Connection Details', text: 'Click on any tag node to open the Connection Details sidebar. It shows stats like note count, link density, and a list of all connected notes.' },
    ]
  },
  {
    id: 'favorites',
    icon: Star,
    color: '#EF4444',
    title: 'Favorites',
    subtitle: 'Quick access to important notes',
    content: [
      { heading: 'Mark as Favorite', text: 'Click the star icon (⭐) on any note in the Editor to toggle it as a favorite. Favorited notes appear in the Favorites page for quick access.' },
      { heading: 'Favorites Page', text: 'Navigate to "Favorites" in the sidebar to see all your starred notes. You can search, open, or delete favorites from this page.' },
    ]
  },
  {
    id: 'tips',
    icon: Zap,
    color: '#EC4899',
    title: 'Pro Tips',
    subtitle: 'Get the most out of NoteGraph',
    content: [
      { heading: 'Keyboard Shortcuts', text: 'Use Ctrl+B for bold, Ctrl+I for italic, Ctrl+U for underline while editing notes. Press Enter in rename modals to quickly save.' },
      { heading: 'Use Tags Strategically', text: 'Create tags for topics, not just categories. For example, use "React Hooks" and "State Management" instead of just "React". This creates richer connections in your Note Graph.' },
      { heading: 'Multiple Sections per Note', text: 'A note can belong to multiple sections (tags). This creates cross-connections in your graph, helping you discover relationships between ideas.' },
      { heading: 'Search & Filter', text: 'Use the search bar on the Dashboard and Tags pages to quickly find notes and tags across your workspace.' },
    ]
  },
];

const GuidePage = () => {
  const [expandedStep, setExpandedStep] = useState('workspaces');

  return (
    <div className="guide-page">
      <div className="guide-container">
        {/* Header */}
        <header className="guide-header">
          <div className="guide-header-icon">
            <BookOpen size={28} color="#fff" />
          </div>
          <div>
            <h1 className="guide-title">User Guide</h1>
            <p className="guide-subtitle">Everything you need to know to master NoteGraph</p>
          </div>
        </header>

        {/* Quick Start Banner */}
        <div className="quick-start-banner">
          <div className="qs-content">
            <Sparkles size={20} />
            <div>
              <h3>Quick Start</h3>
              <p>Create a workspace → Add notes → Tag them with sections → Visualize connections in the Note Graph</p>
            </div>
          </div>
          <div className="qs-steps">
            <span className="qs-step"><FolderClosed size={14} /> Workspace</span>
            <ArrowRight size={14} className="qs-arrow" />
            <span className="qs-step"><FileText size={14} /> Notes</span>
            <ArrowRight size={14} className="qs-arrow" />
            <span className="qs-step"><Tag size={14} /> Tags</span>
            <ArrowRight size={14} className="qs-arrow" />
            <span className="qs-step"><Share2 size={14} /> Graph</span>
          </div>
        </div>

        {/* Guide Sections */}
        <div className="guide-sections">
          {guideSteps.map((step) => {
            const Icon = step.icon;
            const isExpanded = expandedStep === step.id;
            return (
              <div key={step.id} className={`guide-section card ${isExpanded ? 'expanded' : ''}`}>
                <div 
                  className="guide-section-header" 
                  onClick={() => setExpandedStep(isExpanded ? null : step.id)}
                >
                  <div className="gsh-left">
                    <div className="guide-icon-box" style={{background: `${step.color}15`, color: step.color}}>
                      <Icon size={20} />
                    </div>
                    <div className="gsh-text">
                      <h2>{step.title}</h2>
                      <span>{step.subtitle}</span>
                    </div>
                  </div>
                  <div className="gsh-right">
                    <span className="step-count">{step.content.length} steps</span>
                    <ChevronDown 
                      size={18} 
                      className="gsh-chevron" 
                      style={{transform: isExpanded ? 'rotate(180deg)' : 'none'}} 
                    />
                  </div>
                </div>

                {isExpanded && (
                  <div className="guide-section-body">
                    {step.content.map((item, idx) => (
                      <div key={idx} className="guide-step-item">
                        <div className="step-number" style={{background: step.color, boxShadow: `0 2px 8px ${step.color}40`}}>
                          {idx + 1}
                        </div>
                        <div className="step-content">
                          <h4>{item.heading}</h4>
                          <p>{item.text}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {/* Footer */}
        <div className="guide-footer">
          <Layers size={16} />
          <span>NoteGraph — A connected note-taking experience</span>
        </div>
      </div>
    </div>
  );
};

export default GuidePage;
