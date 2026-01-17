# Habitate Design System

> **"Quietly powerful, thoughtfully designed, effortless to use"**

## Design Philosophy

Habitate's design system is built around creating a **calm, minimal, human-centric** experience. Our goal is to help users focus on what matters—their habits, health, and connections—without visual noise or distraction.

### Core Principles

1. **Calm Over Loud** - Muted colors, subtle animations, minimal shadows
2. **Function Over Flash** - Every element serves a purpose
3. **Breathable Space** - Generous whitespace and padding
4. **Accessible by Default** - Clear contrast, readable text, touch-friendly targets

---

## Color System

### Primary Palette (Forest Green)
Our primary color is derived from the Habitate logo—a calming forest green that evokes nature, growth, and wellness.

| Token | Light Mode | Dark Mode | Usage |
|-------|------------|-----------|-------|
| `primary` | `#2D5A47` | `#6B9E8C` | Primary actions, focus states |
| `primaryContainer` | `#D4E8E0` | `#1F3D32` | Backgrounds, subtle highlights |
| `onPrimary` | `#FFFFFF` | `#0D1F18` | Text on primary |

### Accent Palette (Warm Amber)
A warm amber accent adds personality without competing with the primary green.

| Token | Light Mode | Dark Mode | Usage |
|-------|------------|-----------|-------|
| `accent` | `#B8956A` | `#CFA06A` | Highlights, badges, special actions |
| `accentContainer` | `#F5EEE6` | `#3D3226` | Accent backgrounds |

### Semantic Colors
All semantic colors are intentionally muted to avoid alarming the user.

| Token | Light Mode | Dark Mode | Usage |
|-------|------------|-----------|-------|
| `success` | `#5A8A72` | `#7AAF94` | Positive states, completed tasks |
| `warning` | `#C4956B` | `#D4A574` | Caution states, pending items |
| `error` | `#B56B6B` | `#CF8A8A` | Error states (calm, not alarming) |
| `info` | `#6B8AAF` | `#8AA5C4` | Informational states |

### Surface & Background
| Token | Light Mode | Dark Mode | Usage |
|-------|------------|-----------|-------|
| `background` | `#FAFAF8` | `#0F1412` | App background |
| `surface` | `#FFFFFF` | `#161D1A` | Cards, sheets |
| `surfaceVariant` | `#F5F5F3` | `#1E2724` | Secondary surfaces |

---

## Typography

### Font Family
**Google Sans Flex** (or system default fallback)

### Scale

| Style | Size | Weight | Line Height | Usage |
|-------|------|--------|-------------|-------|
| `ScreenTitle` | 28sp | Medium (500) | 1.3 | Main screen headers |
| `SectionTitle` | 20sp | Medium (500) | 1.35 | Section headers, card titles |
| `CardTitle` | 16sp | Medium (500) | 1.4 | List items, card headings |
| `BodyText` | 15sp | Normal (400) | 1.5 | Primary content |
| `SupportingText` | 14sp | Normal (400) | 1.5 | Secondary content |
| `MetaText` | 13sp | Normal (400) | 1.45 | Labels, metadata |
| `CaptionText` | 12sp | Normal (400) | 1.4 | Timestamps, helper text |
| `ButtonText` | 14sp | Medium (500) | 1.3 | Button labels |

### Typography Guidelines
- Use **Medium (500)** weight sparingly—for titles and emphasis only
- Body text should always be **Normal (400)** for readability
- Line height ratios of 1.5 or higher for body text
- Never use bold (700) in the UI—keep things calm

---

## Spacing System

### Base Scale
```
xxs = 2dp    // Tight spacing
xs  = 4dp    // Minimal gaps
sm  = 8dp    // Small gaps
md  = 12dp   // Default gaps
lg  = 16dp   // Component spacing
xl  = 24dp   // Section spacing
xxl = 32dp   // Large section spacing
xxxl = 48dp  // Screen padding
```

### Common Patterns
| Context | Value |
|---------|-------|
| Screen horizontal padding | 20dp |
| Card internal padding | 16dp |
| Card compact padding | 12dp |
| List item vertical padding | 14dp |
| Between form fields | 16dp |

---

## Elevation & Shadows

**Principle: Flatten the UI**

Habitate uses minimal elevation. Most surfaces appear flat with subtle borders rather than shadows.

| Level | Value | Usage |
|-------|-------|-------|
| `none` | 0dp | Default for most surfaces |
| `whisper` | 0.5dp | Very subtle lift |
| `xs` | 1dp | Cards, subtle elevation |
| `sm` | 2dp | FABs, max for cards |
| `md` | 4dp | Dialogs, sheets (rare) |
| `lg` | 8dp | Reserved for modals |

### Guidelines
- Cards should use `xs` (1dp) or `none`
- FABs should use `sm` (2dp)
- Never exceed 4dp elevation in feed content
- Prefer borders over shadows when possible

---

## Corner Radius

| Token | Value | Usage |
|-------|-------|-------|
| `xs` | 4dp | Chips, small elements |
| `sm` | 8dp | Input fields, tags |
| `md` | 12dp | Default cards |
| `lg` | 16dp | Featured cards, images |
| `xl` | 24dp | Modals, bottom sheets |
| `pill` | 9999dp | Pills, avatars, search bars |

---

## Component Guidelines

### Buttons

| Type | Use Case |
|------|----------|
| **Primary** | Single main action per screen |
| **Secondary** | Alternative actions |
| **Text** | Tertiary actions, cancel |
| **Tonal** | Softer emphasis than primary |
| **Icon** | Icon-only actions (44dp min target) |

**Touch Targets**: All interactive elements must have a minimum 44dp touch target.

### Cards

| Type | Elevation | Border | Use Case |
|------|-----------|--------|----------|
| **Standard** | xs (1dp) | None | Default content container |
| **Outlined** | None | Subtle | Secondary content |
| **Featured** | xs | Accent border | Highlighted content |
| **Glass** | None | None | Over images/gradients |

### Inputs

- Use outlined style (not filled)
- Border changes color on focus
- Show error/success states with color AND text
- Character counts for text areas
- Always provide labels (don't rely on placeholders alone)

### Navigation

- Bottom nav: 72dp height with glass effect
- 5 tabs: Feed, Habitats, Create (FAB), Activity, Profile
- Subtle indicator animation on selection
- Icon size: 24dp (selected), 24dp (unselected)

---

## Animation Guidelines

### Duration
| Type | Duration |
|------|----------|
| Quick feedback | 150ms |
| Standard transitions | 250ms |
| Emphasis animations | 350ms |
| Loading shimmer | 1400ms |

### Easing
- Use `tween` with default easing for most transitions
- Use `spring` with moderate damping for playful interactions
- Never use bouncy springs—keep animations professional

### Principles
- Animations should be subtle and purposeful
- Avoid animations that delay user actions
- Loading states should feel calm, not frantic

---

## Accessibility

### Color Contrast
- All text must meet WCAG AA contrast ratios
- Don't rely on color alone to convey information
- Error states include both color AND text/icon

### Touch Targets
- Minimum 44dp × 44dp for all interactive elements
- Include adequate spacing between touch targets

### Screen Readers
- All images must have content descriptions
- Interactive elements must have meaningful labels
- Use semantic heading hierarchy

---

## Dark Mode

Dark mode follows the same principles with adjusted colors:

- Background: Deep charcoal (`#0F1412`) with green undertone
- Surfaces: Slightly elevated (`#161D1A`)
- Text: Warm off-white (`#E8E8E6`) to reduce eye strain
- Primary colors: Slightly more saturated for visibility
- Maintain same elevation and shadow patterns

---

## Do's and Don'ts

### Do ✓
- Use generous whitespace
- Keep elevation minimal (1-2dp)
- Use muted semantic colors
- Animate subtly and purposefully
- Test with actual users for readability

### Don't ✗
- Use bright, saturated colors for errors
- Add shadows to everything
- Use bold text for body content
- Create flashy, attention-grabbing animations
- Sacrifice accessibility for aesthetics

---

## File Reference

| File | Purpose |
|------|---------|
| `HabitateColors.kt` | Color system and themes |
| `Type.kt` | Typography scale and styles |
| `DesignTokens.kt` | Spacing, sizing, elevation, animation |
| `Shape.kt` | Corner radii and shapes |
| `Buttons.kt` | Button components |
| `Cards.kt` | Card components |
| `Navigation.kt` | Navigation components |
| `States.kt` | Empty, loading, error states |
| `Inputs.kt` | Text fields and form elements |

---

*Last updated: December 2025*
