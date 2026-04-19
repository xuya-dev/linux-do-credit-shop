Design a professional admin dashboard with enterprise-grade clarity.

Layout structure:
- Fixed left sidebar: width 240px, background #1e293b (dark slate)
- Top navigation bar: height 60px, background white, border-bottom #e5e7eb
- Main content area: background #f8fafc (light gray), padding 24px

Color system:
- Primary: #3b82f6 (blue) — buttons, links, active states only
- Semantic colors (use low-saturation backgrounds for badges):
  success: text #059669, bg #d1fae5
  warning: text #d97706, bg #fef3c7
  error: text #dc2626, bg #fee2e2
  info: text #2563eb, bg #dbeafe
- Neutral scale: #111827 / #374151 / #6b7280 / #9ca3af / #e5e7eb / #f9fafb
- Never use semantic colors as full background fills — only as subtle badge backgrounds

Component rules:
- Cards: background white, border 1px solid #e5e7eb, border-radius 8px, shadow shadow-sm
- Tables: row height 52px, header 40px, alternating row bg transparent/#f9fafb
- Buttons: primary filled, secondary outlined, no gradient buttons
- Form inputs: border #d1d5db, focus ring #3b82f6 with 2px offset

Typography:
- Page title: 20px/700
- Section label: 11px/600, uppercase, letter-spacing 0.08em, color #6b7280
- Table cell: 14px/400
- Caption/helper: 12px/400, color #9ca3af

Spacing rhythm:
- Card internal padding: 24px
- Section gap: 24px
- Table row padding: 12px 16px

Table behavior:
- Column headers: sticky, background #f9fafb, border-bottom 2px solid #e5e7eb
- Row hover: background #f1f5f9
- Action column: always fixed-width, right-aligned, contains icon buttons only
- Status column: always badge component, never plain text