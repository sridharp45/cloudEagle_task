# cloudEagle_task

The project implements the OAuth2 authorization flow to securely access Dropbox team data. When a user clicks the “Connect to Dropbox” button, they are redirected to Dropbox’s authorization page to grant access to the application. After the user approves, Dropbox redirects back to the application with an authorization code. The backend then exchanges this code for an access token, which is used to make authenticated requests to the https://api.dropboxapi.com/2/team/get_info endpoint. This flow ensures secure handling of credentials, proper token management, and enables the application to fetch and display team information on the UI.

I have tested the following API:

API: https://api.dropboxapi.com/2/team/get_info (used to get information about the team)

After successful authorization, the API response is displayed on the UI.

<img width="1434" height="827" alt="Screenshot 2025-09-28 at 8 00 44 PM" src="https://github.com/user-attachments/assets/bfb88a35-0ae4-4e96-8e02-eb86efa6a2af" />

<img width="1434" height="827" alt="Screenshot 2025-09-28 at 8 00 48 PM" src="https://github.com/user-attachments/assets/d82ae124-7d43-4536-863d-3c309f815abf" />

<img width="1434" height="827" alt="Screenshot 2025-09-28 at 8 12 37 PM" src="https://github.com/user-attachments/assets/987cd4ac-f440-4eb4-87ea-df8c56b50d63" />

