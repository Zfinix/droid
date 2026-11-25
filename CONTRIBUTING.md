# Contributing

Bug reports and small fixes are welcome.

- **Bugs:** [open an issue](https://github.com/Zfinix/droid/issues) with your phone model, Android version and Droid version (in Settings). The template asks for the rest.
- **Security:** see [SECURITY.md](SECURITY.md), not a public issue.
- **Code:** for anything bigger than a fix, open an issue first so we can agree on the shape. Building from source is in [docs/BUILDING.md](docs/BUILDING.md), and the CLI has its own tests: `cd cli && npm install && npm test`.

Follow the style of the file you are in. Commit messages use [Conventional Commits](https://www.conventionalcommits.org): `fix(cli): read the keys over adb`.

Most of the agent's behavior lives in Markdown, not Kotlin: `device-AGENTS.md` and `skills/`. A change there needs no new code.
