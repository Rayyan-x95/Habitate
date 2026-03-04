Habitate Design System — Modern Edition

“Focused, fluid, and quietly intelligent.”

Habitate’s design language blends minimalism, warmth, and clarity to support users building habits, maintaining wellbeing, and connecting with communities.

The interface should feel effortless, calm, and modern — never overwhelming.

1. Design Philosophy

Habitate follows modern calm technology principles.

Core Principles
1. Clarity Over Complexity

Interfaces should communicate information clearly with minimal visual noise.

2. Calm Interaction

Animations, transitions, and colors should feel natural and soothing.

3. Structured Minimalism

Minimal does not mean empty — every element must serve a purpose.

4. Depth Through Layers

Use subtle surfaces, elevation, and spacing rather than heavy shadows.

5. Human-Centered Interfaces

Design must prioritize readability, accessibility, and comfortable interaction.

2. Color System

Habitate’s palette is inspired by nature and wellbeing.

The colors are intentionally muted, earthy, and balanced.

Primary Color System (Forest Core)

The primary color evokes growth, stability, and calm focus.

Token	Light	Dark	Usage
primary	#2D5A47	#6B9E8C	Primary actions
primaryContainer	#D4E8E0	#1F3D32	Containers
onPrimary	#FFFFFF	#0D1F18	Text/icons on primary

Usage examples:

• Focus timer
• Primary buttons
• Selected navigation items

Secondary Accent (Warm Earth)

Accent color adds personality without overwhelming the UI.

Token	Light	Dark	Usage
accent	#B8956A	#CFA06A	Highlights
accentContainer	#F5EEE6	#3D3226	Badges

Used for:

• Achievements
• Highlights
• Community badges

Semantic Colors

Semantic states must feel informative but not alarming.

Token	Light	Dark	Usage
success	#5A8A72	#7AAF94	Completed states
warning	#C4956B	#D4A574	Pending items
error	#B56B6B	#CF8A8A	Errors
info	#6B8AAF	#8AA5C4	Information

Guideline:
Never use bright red or harsh colors.

3. Surface System

Surfaces define visual hierarchy.

Token	Light	Dark
background	#FAFAF8	#0F1412
surface	#FFFFFF	#161D1A
surfaceVariant	#F5F5F3	#1E2724
outline	#E0E0DD	#2A332F
4. Typography

Typography is designed for clarity and comfort.

Font Stack

Primary font:

Google Sans Flex

Fallbacks:

• Inter
• System sans-serif

Typography Scale
Token	Size	Weight	Usage
Display	32sp	500	Hero titles
ScreenTitle	28sp	500	Main screens
SectionTitle	20sp	500	Sections
CardTitle	16sp	500	Cards
Body	15sp	400	Body text
Supporting	14sp	400	Secondary text
Meta	13sp	400	Labels
Caption	12sp	400	Metadata
Button	14sp	500	Buttons
Typography Rules

✓ Avoid bold (700) in most cases
✓ Use medium (500) sparingly
✓ Maintain line-height ≥ 1.4 for readability
✓ Avoid dense text blocks

5. Spacing System

Spacing defines visual rhythm.

Base spacing grid = 4dp

xxs = 2dp
xs  = 4dp
sm  = 8dp
md  = 12dp
lg  = 16dp
xl  = 24dp
xxl = 32dp
xxxl = 48dp
Layout Patterns
Pattern	Value
Screen padding	20dp
Card padding	16dp
Compact card padding	12dp
List item vertical	14dp
Form spacing	16dp

Whitespace is critical.

Avoid cramped layouts.

6. Elevation System

Habitate prioritizes flat surfaces with subtle depth.

Token	Value	Usage
none	0dp	Default
soft	1dp	Cards
raised	2dp	FAB
dialog	4dp	Dialogs
modal	8dp	Bottom sheets

Guidelines:

• Cards should not exceed 1dp elevation
• Avoid layered shadows

7. Corner Radius

Rounded shapes create a softer feel.

Token	Value
xs	4dp
sm	8dp
md	12dp
lg	16dp
xl	24dp
pill	9999dp

Use cases:

• Cards → md
• Inputs → sm
• Modals → xl
• Chips → pill

8. Component System
Buttons

Button hierarchy:

Primary
Secondary
Tonal
Text
Icon

Button rules

Primary buttons should appear once per screen.

Minimum height:

44dp

Cards

Cards are primary containers.

Types:

Type	Use
Standard	Feed items
Outlined	Secondary containers
Featured	Important content
Glass	Overlay content
Inputs

Forms should prioritize clarity.

Rules:

✓ Always show labels
✓ Never rely only on placeholders
✓ Show error and helper text

Input height:

48dp minimum

Navigation

Bottom navigation:

Height: 72dp

Tabs:

• Feed
• Habitats
• Create
• Activity
• Profile

Create is centered FAB.

Icons:

24dp.

9. Motion & Animation

Motion should feel natural and supportive.

Durations
Motion	Duration
Micro feedback	120–150ms
Standard transitions	220–250ms
Emphasis motion	320–350ms
Loading shimmer	1200–1400ms
Animation Rules

Animations should:

✓ Guide attention
✓ Confirm actions
✓ Maintain flow

Avoid flashy effects.

10. Accessibility

Habitate must be accessible by default.

Contrast

All text must meet WCAG AA.

Touch Targets

Minimum size:

44dp × 44dp

Screen Readers

Ensure:

• All icons have descriptions
• Inputs have labels
• Headings follow hierarchy

11. Dark Mode

Dark mode uses warm, eye-friendly tones.

Avoid pure black.

Primary adjustments:

Element	Value
Background	#0F1412
Surface	#161D1A
Text	#E8E8E6

Primary colors become slightly more saturated.

12. Interaction Principles

Habitate interactions should feel:

Calm
Predictable
Responsive

Examples:

• Like animations subtle
• Transitions smooth
• Loading states calm

13. Do's and Don'ts
Do

✓ Use generous whitespace
✓ Maintain consistent spacing
✓ Keep animations subtle
✓ Use calm color palette

Don’t

✗ Use bright error colors
✗ Overuse shadows
✗ Use heavy bold text
✗ Add flashy animations

14. Design Tokens Structure
File	Purpose
HabitateColors.kt	Color system
Type.kt	Typography
Shapes.kt	Corner radius
DesignTokens.kt	Spacing, elevation
Buttons.kt	Button components
Cards.kt	Card components
Inputs.kt	Text fields
Navigation.kt	Navigation UI
States.kt	Loading, empty, error states
15. Design System Goal

The Habitate interface should feel like:

• Calm productivity tool
• Wellbeing companion
• Community platform

All without overwhelming the user.

The result should be a modern, minimal, thoughtful interface that quietly empowers users.