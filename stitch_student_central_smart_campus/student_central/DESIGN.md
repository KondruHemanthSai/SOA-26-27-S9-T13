---
name: Student Central
colors:
  surface: '#f7f9fb'
  surface-dim: '#d8dadc'
  surface-bright: '#f7f9fb'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f4f6'
  surface-container: '#eceef0'
  surface-container-high: '#e6e8ea'
  surface-container-highest: '#e0e3e5'
  on-surface: '#191c1e'
  on-surface-variant: '#464554'
  inverse-surface: '#2d3133'
  inverse-on-surface: '#eff1f3'
  outline: '#767586'
  outline-variant: '#c7c4d7'
  surface-tint: '#494bd6'
  primary: '#4648d4'
  on-primary: '#ffffff'
  primary-container: '#6063ee'
  on-primary-container: '#fffbff'
  inverse-primary: '#c0c1ff'
  secondary: '#565e74'
  on-secondary: '#ffffff'
  secondary-container: '#dae2fd'
  on-secondary-container: '#5c647a'
  tertiary: '#006577'
  on-tertiary: '#ffffff'
  tertiary-container: '#008096'
  on-tertiary-container: '#f9fdff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e1e0ff'
  primary-fixed-dim: '#c0c1ff'
  on-primary-fixed: '#07006c'
  on-primary-fixed-variant: '#2f2ebe'
  secondary-fixed: '#dae2fd'
  secondary-fixed-dim: '#bec6e0'
  on-secondary-fixed: '#131b2e'
  on-secondary-fixed-variant: '#3f465c'
  tertiary-fixed: '#acedff'
  tertiary-fixed-dim: '#4cd7f6'
  on-tertiary-fixed: '#001f26'
  on-tertiary-fixed-variant: '#004e5c'
  background: '#f7f9fb'
  on-background: '#191c1e'
  surface-variant: '#e0e3e5'
typography:
  display-lg:
    fontFamily: Geist
    fontSize: 48px
    fontWeight: '600'
    lineHeight: 56px
    letterSpacing: -0.04em
  headline-lg:
    fontFamily: Geist
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Geist
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Geist
    fontSize: 24px
    fontWeight: '500'
    lineHeight: 32px
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: JetBrains Mono
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 4px
  container-max: 1280px
  gutter: 24px
  margin-mobile: 16px
  margin-desktop: 40px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 32px
---

## Brand & Style

The design system embodies a "Digital Campus" philosophy: a space that feels as prestigious as a physical university library yet as frictionless as a high-performance developer tool. It targets Gen-Z students and modern faculty who expect professional-grade software for their academic lives.

The aesthetic is **Modern-Academic Minimalism**—a blend of high-utility SaaS (Linear/Vercel) and premium editorial design. The UI prioritizes focus through generous whitespace, high-fidelity glassmorphism, and a disciplined application of color. It avoids the cluttered, "portal" look of legacy educational software in favor of a unified, intelligent workspace that feels calm, organized, and technologically advanced.

## Colors

The palette is rooted in a "Deep Academic" foundation with vibrant "Intelligence" accents. 

- **Base Layers:** Use `neutral` (off-white) for the main canvas in light mode, or a custom deep navy (`secondary`) for high-contrast dark modes.
- **Primary Indigo:** Used for main actions and branding, bridging the gap between traditional university blue and modern tech violet.
- **Functional Accents:** `Cyan` is used for information and data visualization, while `Subtle Emerald` is reserved for success states and academic progress indicators.
- **Gradients:** Use soft, multi-stop linear gradients (e.g., Indigo to Cyan) sparingly for headers, progress bars, or active states to imply motion and growth.

## Typography

This design system utilizes a trio of typefaces to establish hierarchy and technical authority. 

1. **Geist (Headlines):** Provides a precise, geometric feel for all headers. Tight letter spacing is required for larger sizes to maintain the "Linear" aesthetic.
2. **Inter (Body):** The workhorse for all reading experiences, course materials, and UI text. High legibility and a neutral tone keep the focus on content.
3. **JetBrains Mono (Labels/Metadata):** Used for tags, dates, and technical metadata (like course codes or credit counts) to provide a "built-for-builders" academic feel.

## Layout & Spacing

The layout follows a **Fluid Intelligence** model. Content is organized in a 12-column grid for desktop, moving to a 1-column stack for mobile. 

- **Whitespace:** Use generous margins to prevent the "dashboard fatigue" common in EDU apps. Information density should be low by default, with "expert modes" that allow users to condense views.
- **Rhythm:** All spacing must be multiples of the 4px base unit. 
- **The "Command Center" Layout:** Primary navigation is positioned in a slim left-hand sidebar (collapsible), while secondary actions are located in a glassmorphic top bar.

## Elevation & Depth

This design system uses **Layered Glassmorphism** to communicate hierarchy.

- **Level 0 (Canvas):** The base background color.
- **Level 1 (Cards/Panels):** Surface with 40-60% opacity, a 16px backdrop blur, and a 1px "inner glow" border (white at 10% opacity).
- **Level 2 (Modals/Popovers):** Higher opacity (80%), 32px backdrop blur, and a soft "ambient" shadow: `0 20px 50px rgba(0,0,0,0.1)`.
- **Glow Effects:** Active elements or "intelligent" features (like AI suggestions) may utilize a subtle background radial gradient glow in Indigo or Cyan to draw the eye without using high-contrast borders.

## Shapes

The shape language is "Soft-Tech." While the core grid is rigid, the elements within it feature large, friendly radii.

- **Standard Elements:** Buttons, inputs, and small chips use `rounded-md` (8px).
- **Containers:** Dashboard cards, course modules, and main content areas use `rounded-xl` (24px) to create a soft, modern enclosure.
- **Interactive States:** Hovering over a card should trigger a slight scale-up (1.02x) and an increase in the backdrop blur intensity.

## Components

- **Glass Cards:** The signature component. Must include a `1px` border using a low-opacity version of the primary color. Backgrounds use `blur(16px)`.
- **Primary Buttons:** Solid Indigo or a subtle Gradient (Indigo to Violet). No heavy drop shadows; use a subtle "glow" on hover. Text is always Semibold.
- **Ghost Buttons:** Transparent background with the 1px border logic. Text uses the Primary color.
- **Course Chips:** Used for categorization. High-contrast background (e.g., Pale Cyan background with Dark Cyan text) using JetBrains Mono for the label.
- **Data Visualization:** Charts should use rounded line caps and "glow" lines. Background fills for area charts should be semi-transparent gradients.
- **Input Fields:** Minimalist. Underline-only or very subtle 1px bordered boxes that highlight with a Cyan glow when focused.
- **Command Menu:** A central "CMD+K" style search and action bar, floating with high elevation and maximum glassmorphism.