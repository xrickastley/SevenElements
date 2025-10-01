import { defineConfig, HeadConfig } from 'vitepress';

// https://vitepress.dev/reference/site-config
export default defineConfig({
  base: '/SevenElements/wiki',
  title: "Seven Elements Docs",
  description: "Documentation for the Seven Elements Minecraft mod.",
  head: [['link', { rel: 'icon', type: 'image/png', href: '/SevenElements/wiki/icon.png' }]],
  lastUpdated: true,
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: 'Home', link: '/' }
    ],

    search: {
      provider: 'local'
    },

    logo: '/SevenElements/wiki/icon.png',

    sidebar: {
      "guide": [
        { text: 'Getting Started', link: '/guide' },
        {
          text: 'Workstations',
          items: [
            { text: 'Infusion Table', link: '/guide/workstations/infusion_table' }
          ]
        },
        {
          text: 'Elements',
          items: [
            { text: 'The Seven Elements', link: '/guide/elements/the_seven_elements' },
            { text: 'Elemental Combat', link: '/guide/elements/elemental_combat' },
            { text: 'Elemental Reactions', link: '/guide/elements/elemental_reactions' },
            { text: 'Elemental Gauge Theory', link: '/guide/elements/elemental_gauge_theory' },
            { text: 'Internal Cooldown', link: '/guide/elements/internal_cooldown' }
          ]
        },
        {
          text: 'Miscellaneous',
          items: [
            { text: 'Commands', link: '/guide/misc/commands' },
            { text: 'Configuration', link: '/guide/misc/configuration' },
            { text: 'Game rule', link: '/guide/misc/game_rule' }
          ]
        }
      ],
      "developer": [
        { text: 'Getting Started', link: '/developer' },
        {
          text: 'Data pack',
          items: [
            { text: 'ICD Type definition', link: '/developer/data_pack/internal_cooldown_type_definition' },
            { text: 'Damage type tag', link: '/developer/data_pack/damage_type_tag' },
            { text: 'Entity type tag', link: '/developer/data_pack/entity_type_tag' },
            { text: 'Item tag', link: '/developer/data_pack/item_tag' }
          ]
        },
        {
          text: 'Mod',
          items: [
            { text: 'Disabling Entity Elements', link: '/developer/mod/disabling_entity_elements' },
            { text: 'Adding an Elemental Reaction', link: '/developer/mod/adding_an_elemental_reaction' },
            { text: 'Events', link: '/developer/mod/events' }
          ]
        },
        {
          text: 'Compatibility',
          items: [
            { text: 'Fixing Elemental Infusions', link: '/developer/compatibility/fixing_elemental_infusions' },
            { text: 'Fixing Boss Bar Displays', link: '/developer/compatibility/fixing_boss_bar_displays' },
          ]
        }
      ]
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/xrickastley/SevenElements/tree/wiki' }
    ],

    outline: {
      level: [2, 3]
    },

    footer: {
      message: "THIS PROJECT IS NOT AFFILIATED WITH NEITHER HOYOVERSE NOR GENSHIN IMPACT.",
      copyright: "© All rights reserved by HoYoverse. Other properties belong to their respective owners. | Docs released under the <a href=\"https://github.com/xrickastley/SevenElements/tree/docs/LICENSE\">CC BY-NC-SA License</a>",
    }
  },
  markdown: {
    math: true,
    theme: {
      dark: 'dark-plus',
      light: 'light-plus'
    },
    image: {
      lazyLoading: true
    },
    languages: [
      async () =>
        await import("syntax-mcfunction/mcfunction.tmLanguage.json", {
          with: { type: "json" },
        }).then(lang => ({ ...(lang.default as any), name: "mcfunction" })),
    ]
  },
  transformHead({ assets }) {
    const genshinFont = assets.find(f => /Genshin\.[\w]+\.ttf/.test(f));
    const config: HeadConfig[] = [];

    if (genshinFont) config.push([
      "link",
      { rel: 'preload', href: genshinFont, as: 'font', type: 'font/ttf', crossorigin: '' }
    ])

    return config;
  },
})
