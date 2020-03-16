# Clojure Diff Action (using [autochrome](https://github.com/ladderlife/autochrome))

Runs autochrome on PR diffs and adds a comment to the PR with a link to the structurual diff created by autochrome.

- Currently this relies on an external service that stores and serves the resulting diff (HTML file). This service is maintained as a Firebase function in `functions/`.
- :warn: Because this service stores diffs in a publicly readable way (if you know the URL) it is probably best not to use this action with proprietary code.
- A minimal example PR can be found here: https://github.com/martinklepsch/autochrome-action/pull/2

### Things that could be improved

- Update the PR description with a link to the diff instead of adding comment add the end of the PR thread.
- Adjust the design of autochrome's diffs to be less different to GitHub's diffs.
- Potentially ignore non-Clojure code in autochrome diffs.
- More? [Open an issue!](https://github.com/martinklepsch/autochrome-action/issues/new)

# Usage

```yaml
    steps:
      - name: Checkout Repo
        uses: actions/checkout@master
        with:
          fetch-depth: 0       # <<< important
      - name: Run Autochrome
        uses: martinklepsch/autochrome-action@master
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
```

# License

The scripts and documentation in this project are released under the [MIT License](LICENSE)
