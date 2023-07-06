# mechanize-app
Mechanize (app) to help people fix car and reach destination.

## Necessary
You need to set `API_URL_ACCOUNTS`, `API_URL_HELPS`, `CLIENT_ID`, `SHARED_PREF_KEY` and `MAPS_API_KEY` variables in `local.properties`.

## See more
- [API's](https://github.com/tech-warriors-corporation/mechanize-api).

## Deploy
You must build the app before of generate a `.aab` file.

## Roadmap
- [X] Add maps;
- [ ] Make each screen by [Figma](https://www.figma.com/proto/kl05E88sullmKzVTNxXItO/Mechanize?node-id=2-2&scaling=scale-down&page-id=0%3A1&starting-point-node-id=2%3A2);
- [ ] Set the Mechanize project as a case;
- [ ] Remove all delays in source code;
- [ ] Show mechanic name to driver when accepted the service;
- [ ] Add a message to comment about service on rating;
- [ ] Get pending rating or attendance unfinished on entering in application;
- [ ] Show modal in the system informing that we are not responsible for payments, this is something directly between driver and mechanic;
- [X] Add Tech Warriors logo in a page;
- [X] Use location;
- [ ] Review all project files;
- [ ] Review application with team;
- [ ] Improve product always;
- [X] See password with button on type;
- [X] Put a button with return to focus me in map;
- [ ] Delete unused files;
- [ ] Customize theme;
- [X] Hide environment variables in `app/build.gradle`;
- [X] Send access token in headers request;
- [X] Study about elevation (shadow);
- [ ] Use Android notifications;
- [ ] Think about product marketing;
- [X] Mock the API's;
- [ ] If user tries to enter the password 5 times and fails, then it must wait 15 seconds to try again;
- [ ] Use singular and plural in seconds of retry login, and not disabled in first time;
- [ ] Upload application in Google Play;
- [ ] Customize application in Google Play when uploaded;
- [X] Test in others devices;
- [X] Maybe create a url for each microservice (environment variable);
- [X] Remove TODO comments;
- [ ] Change `MAPS_API_KEY` value;
- [ ] Get current ticket after close app and open again;
- [ ] On close modal and open again, shows a ghost of last data;
- [ ] Show in map who mechanic is helping when attending;
- [ ] Add back button in choose ticket to get previous ticket;
- [ ] Set details of ticket in rating modal too (more precision for user);
- [ ] Update location in runtime for driver and mechanic;
- [ ] Set URL of app in GitHub repository;
- [ ] Remove dark theme mode;
- [X] Add padding in images at `mipmap` directory to smaller app icon;
- [ ] Remove -3 hours when show a full date in app or change time in Neon database;
- [ ] Analyze crashes and bugs;
- [ ] Set login fields on home;
- [ ] On start app, request our microservices to init them (15 inactive minutes);
- [ ] Toggle button to show or hide password on create account;
- [ ] Create a confirm modal before logout;
- [ ] Focus me and logout button should have a shadow or something to show better (bring up the map);
- [ ] Set a sanitize to remove emoji and dangerous strings in some fields for not save in database;
- [ ] Add text that go to creator website and Tech Warriors GitHub organization link;
- [ ] Use select to vehicle;
- [ ] Forget password screen;
- [ ] Remember session checkbox;
- [ ] Show remaining chars text;
- [ ] Online mechanics alert;
- [ ] Change password configuration;
- [X] Set link of Google Maps in a button on right when mechanic is attending a driver;
- [X] Stop map pinch on opened modal;
- [X] Close keyboard on change fragment;
- [X] Use a select instead a autocomplete;
- [X] On app start, request to verify token and update id, name and role in EncryptedSharedPreferences;
- [X] Disable buttons on request.
