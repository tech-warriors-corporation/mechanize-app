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
- [ ] Set details of ticket in rating modal too (more precision for user with description, vehicle, mechanic name and more);
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
- [ ] Focus me and logout button should have a shadow or something to show better (bring up the map). The icon button should be like a button and not only icon shape;
- [ ] Set a sanitize to remove emoji and dangerous strings in some fields for not save in database;
- [ ] Add text that go to creator website and Tech Warriors GitHub organization link;
- [ ] Use select to vehicle;
- [ ] Forget password screen;
- [ ] Remember session checkbox;
- [ ] Show remaining chars text;
- [ ] Online mechanics alert;
- [ ] Change password configuration;
- [ ] Set text in info of app about keep the app open to work normally;
- [ ] Test app with location changed in other country (emulate);
- [ ] When mechanic recuse (cancel button name should be recuse) the ticket, should be unsolved and without mechanic_id, and not with cancelled;
- [ ] Delete account option;
- [ ] Change snippet of location in map for an engine, person or car;
- [ ] More options to configure, historic and others;
- [ ] Show the path traced of mechanic between driver;
- [ ] Rating modal to optional and can close to users;
- [ ] Rating modal for the mechanic too;
- [ ] Text that showing vision of other user;
- [X] Toast time should be bigger and the toast should be at top of app. The toast should have a bigger height for large messages;
- [ ] Double confirmation in actions (principally in cancel and conclude);
- [ ] Show the services to mechanic that is closer than mechanic and not let services appear that are more than 300km from him;
- [ ] Add list of done services to users with status and other information;
- [ ] Allow mechanic be driver and and vice versa;
- [ ] Remove rating modal on cancel;
- [ ] App should work in background when closed and logged in;
- [ ] Improve texts and labels to better descriptions;
- [ ] Show current position of mechanic to driver and others relevant things;
- [ ] Update location of users when them leave the place, and show two points in map (driver and mechanic) in different colors;
- [ ] Improve message errors in app;
- [ ] Use trim in strings to validate fields in ifs;
- [X] Set link of Google Maps in a button on right when mechanic is attending a driver (something better to understand and beautiful);
- [X] Stop map pinch on opened modal;
- [X] Close keyboard on change fragment;
- [X] Use a select instead a autocomplete;
- [X] On app start, request to verify token and update id, name and role in EncryptedSharedPreferences;
- [X] Disable buttons on request.
