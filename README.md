# Memorize It

Memorize It includes a browser version of a personalized brain training puzzle app designed around familiar personal memories.
Users can upload personal photos, choose a grade from 5 to 10 photos, and turn those into a memory matching game.

## Features

- Profile setup with name and grade selection from 5 to 10 photos
- Photo upload from device gallery
- Local profile persistence using localStorage
- Personalized memory match puzzle using uploaded photos only

## Platforms

- Web app: HTML/CSS/JavaScript in `web/`

## Project Structure

- web/index.html: profile setup and puzzle layout
- web/styles.css: visual styling
- web/script.js: profile persistence and puzzle logic

## Build and Run

### Web

1. Open `web/index.html` in a browser, or run a local static server.
2. Optional local server from workspace root: `python -m http.server 8080`
3. Open `http://localhost:8080/web/`

## Notes for Dementia-Friendly Iteration

The current version is a functional baseline. For production use with dementia patients, consider:

- Larger fonts and higher contrast options
- Optional audio prompts and voice instructions
- Caregiver mode for content setup
- Session analytics for progress tracking
- Calmer pacing and reduced time pressure
