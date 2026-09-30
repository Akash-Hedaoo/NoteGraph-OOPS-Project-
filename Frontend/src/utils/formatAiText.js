/**
 * Lightweight Markdown → HTML converter for AI chatbot output.
 * Handles: headings, bold, italic, inline code, fenced code blocks,
 * unordered/ordered lists, blockquotes, horizontal rules, links, and paragraphs.
 *
 * This avoids pulling in a full markdown library (react-markdown + remark)
 * and keeps the bundle size small.
 */

export function formatAiText(text) {
  if (!text) return '';

  let html = text;

  // 1. Escape HTML entities (security)
  html = html
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');

  // 2. Fenced code blocks: ```lang\n...\n```
  html = html.replace(/```(\w*)\n([\s\S]*?)```/g, (_, lang, code) => {
    const langLabel = lang ? `<span class="ai-code-lang">${lang}</span>` : '';
    return `<div class="ai-code-block">${langLabel}<pre><code>${code.trim()}</code></pre></div>`;
  });

  // 3. Inline code: `code`
  html = html.replace(/`([^`\n]+)`/g, '<code class="ai-inline-code">$1</code>');

  // 4. Headings: ### Heading
  html = html.replace(/^#### (.+)$/gm, '<h4 class="ai-heading">$1</h4>');
  html = html.replace(/^### (.+)$/gm, '<h3 class="ai-heading">$1</h3>');
  html = html.replace(/^## (.+)$/gm, '<h2 class="ai-heading">$1</h2>');
  html = html.replace(/^# (.+)$/gm, '<h1 class="ai-heading">$1</h1>');

  // 5. Bold + Italic: ***text*** or ___text___
  html = html.replace(/\*\*\*(.+?)\*\*\*/g, '<strong><em>$1</em></strong>');

  // 6. Bold: **text** or __text__
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
  html = html.replace(/__(.+?)__/g, '<strong>$1</strong>');

  // 7. Italic: *text* or _text_
  html = html.replace(/(?<!\w)\*(?!\s)(.+?)(?<!\s)\*(?!\w)/g, '<em>$1</em>');
  html = html.replace(/(?<!\w)_(?!\s)(.+?)(?<!\s)_(?!\w)/g, '<em>$1</em>');

  // 8. Blockquotes: > text
  html = html.replace(/^&gt; (.+)$/gm, '<blockquote class="ai-blockquote">$1</blockquote>');

  // 9. Horizontal rule: --- or ***
  html = html.replace(/^(---|___|\*\*\*)$/gm, '<hr class="ai-hr" />');

  // 10. Links: [text](url)
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer" class="ai-link">$1</a>');

  // 11. Unordered lists: - item or * item
  html = html.replace(/^(?:[-*]) (.+)$/gm, '<li class="ai-li">$1</li>');
  html = html.replace(/((?:<li class="ai-li">.*<\/li>\n?)+)/g, '<ul class="ai-ul">$1</ul>');

  // 12. Ordered lists: 1. item
  html = html.replace(/^\d+\. (.+)$/gm, '<li class="ai-li-ordered">$1</li>');
  html = html.replace(/((?:<li class="ai-li-ordered">.*<\/li>\n?)+)/g, '<ol class="ai-ol">$1</ol>');

  // 13. Paragraphs: wrap remaining lines
  // Split by double newlines to find paragraph boundaries
  html = html
    .split(/\n{2,}/)
    .map(block => {
      const trimmed = block.trim();
      // Don't wrap already-wrapped block elements
      if (
        trimmed.startsWith('<h') ||
        trimmed.startsWith('<ul') ||
        trimmed.startsWith('<ol') ||
        trimmed.startsWith('<div') ||
        trimmed.startsWith('<blockquote') ||
        trimmed.startsWith('<hr') ||
        trimmed.startsWith('<pre')
      ) {
        return trimmed;
      }
      if (trimmed) {
        // Convert single newlines to <br> within a paragraph
        return `<p class="ai-p">${trimmed.replace(/\n/g, '<br />')}</p>`;
      }
      return '';
    })
    .join('\n');

  return html;
}
