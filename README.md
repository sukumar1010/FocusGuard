  **About FocusGuard**
FocusGuard is an Android productivity app designed to help users limit distractions by temporarily blocking access to selected applications.
Using Accessibility Services and an intuitive time-based restriction interface, the app enforces "focus sessions" where selected apps become 
inaccessible for a user-defined duration.

**🎯 Features Based on Your Video:**
**1.App Selection Interface:**

    1.Displays a list of installed apps with toggle switches.
  
    2.Users can choose which apps to block during a focus session.

**2.Duration Setup:**

    1.Users can select a custom duration for the focus session using a time-selector UI.
    
    2.Optionally shows countdown in minutes and seconds.

**3.Start Session:**

    1.Once the time is set, clicking "Restrict App Access" starts the timer and blocks the selected apps.

    2.A toast message confirms the session has started.

**4.Blocking Mechanism:**

    1.Uses an Accessibility Service to monitor foreground apps.
    
    2.If a blocked app is opened during the session, the app is instantly closed or a blocking overlay is shown.

**5.Real-Time Countdown:**

    1.Displays remaining time during the session on the main screen.

**6.Toggle Locking:**

    1.Switches for app selection are disabled during an active session.

    2.Users cannot turn off blocking until the countdown ends.

**7.Session Persistence:**

    1.Timer and block state are saved using SharedPreferences.

    2.Even after app is closed and reopened, the session continues until time expires.

**Sample Video**


https://github.com/user-attachments/assets/a4b4076d-f44b-414d-af84-c41611c16ba0


