======
AXEMAS
======

Development Framework for MultiPlatform hybrid mobile applications.

Core Concepts
=============

AXEMAS handles the whole navigation of the application and transition between views, 
while it permits to implement the views content in HTML itself.

AXEMAS works using ``sections``, each ``Section`` represents the content of the view
and is loaded from an HTML file or from an external URL.

Whenever native code requires to be attached to a section, it is possible to attach
a ``SectionController`` to a ``Section`` itself.

Declaring Sections
==================

iOS
---

The main application controller (``window.rootViewController``) must be
created using ``[NavigationSectionsManager makeApplicationRootController]``.

``makeApplicationRootController`` accepts an array of section data, each data
will be used to create a tab with a section inside.

To create an application with a stack of sections (using a *Navigation Controller*),
and not tabs, just pass data for a single section data::

    [NavigationSectionsManager makeApplicationRootController:@[@{
            @"url":@"www/index.html",
            @"title":@"Home",
        }]
    ];

The created section will contain content of ``www/index.html`` and will be
titled ``Home``. Further sections can be pushed onto the navigation stack
using ``axemas.goto(dictionary)`` from Javascript. 

To create an application with a TabBar just pass data for multiple sections
into the ``makeApplicationRootController`` array, each section must have an 
``url`` pointing to the section path and can have a ``title`` and ``icon`` which
will be used as title and icon for the TabBar tabs.

An application with sidebar can also be created by passing a section data as
sidebar to the ``makeApplicationRootController``::

    [NavigationSectionsManager 
        makeApplicationRootController:@[@{
            @"url":@"www/index.html",
            @"title":@"Home",
            @"toggleSidebarIcon":@"reveal-icon"}]
        withSidebar:@{@"url":@"www/sidebar.html"}
    ];

The sidebar will be created with content from the section data passed in
``withSidebar`` parameter, sections that have a ``toggleSidebarIcon`` 
value in section data will provide a button to open and close the sidebar
with the given icon. If the value is omitted, even when the sidebar is
enabled, there will be no button to show it.

The ``NavigationSectionsManager`` manages the whole AXEMAS navigation
system, creates the sections and keeps track of the current *Navigation Controller*,
*TabBar Controller* and *Sidebar Controller* which are exposed through
``NavigationSectionsManager`` methods:

    - activeNavigationController
    - activeController
    - activeSidebarController
    - goto
    - pushController

Android
-------

The application's MainActivity must extend ``AXMActivity`` which creates the application main structure. Unlike iOS there is no support for tabs yet.
In the ``onCreate`` of your MainActivity initialize the root section by calling ``NavigationSectionsManager.makeApplicationRootController()``.
``makeApplicationRootController()`` accepts a JSONObject containing the section's data.::


    JSONObject data = new JSONObject();
    try {
        data.put("url", "www/index.html");
        data.put("title", "Home");
    } catch (JSONException e) {
        e.printStackTrace();
    }
    NavigationSectionsManager
            .makeApplicationRootController(this, data);



The created section will contain content of ``www/index.html`` and will be
titled ``Home``. Further sections can be pushed onto the navigation stack
using ``axemas.goto(dictionary)`` from Javascript.

An application with sidebar can also be created by passing a section data as
sidebar to the ``makeApplicationRootController``::

    JSONObject data = new JSONObject();
    try {
        data.put("url", "www/index.html");
        data.put("toggleSidebarIcon", "reveal-icon");
        data.put("title", "Home");
    } catch (JSONException e) {
        e.printStackTrace();
    }
    NavigationSectionsManager
            .makeApplicationRootController(this, data, "www/sidebar.html");

The sidebar will be created with content from the section data passed as
``sidebarURL`` parameter, sections that have a ``toggleSidebarIcon`` 
value in section data will provide a button to open and close the sidebar
with the given icon. If the value is omitted, even when the sidebar is
enabled, there will be no button to show it.

The ``NavigationSectionsManager`` manages the whole AXEMAS navigation
system, creates the sections and keeps track of the current *Fragment Stack*,
*Action Bar* and *Sidebar* which are exposed through
``NavigationSectionsManager`` methods:

    - goTo
    - pushFragment
    - sidebarButtonVisibility
    - toggleSidebar
    - showProgressDialog
    - hideProgressDialog
    - showDismissibleAlertDialog
    - enableBackButton


Section Controllers
===================

Section controllers permit to attach native code to each section,
doing so is as simple as subclassing section controllers and
providing ``sectionWillLoad`` and ``sectionDidLoad`` methods.

Inside those methods it is possible to register additional native
functions on the javascript bridge.

iOS
---

Inside ``viewWillLoad`` method of ``SectionController`` subclass
it is possible to register handlers which will be available
in Javascript using ``axemas.call``::

    @implementation HomeSectionController

    - (void)sectionWillLoad {
        [self.section.bridge registerHandler:@"openMap" handler:^(id data, WVJBResponseCallback responseCallback) {
            UINavigationController *navController = [NavigationSectionsManager activeNavigationController];
            [navController pushViewController:[[MapViewController alloc] init] animated:YES];
            
            if (responseCallback) {
                responseCallback(nil);
            }
        }];
    }

    @end

Registering the ``SectionController`` for a section can be done
using the ``NavigationSectionsManager``::

    [NavigationSectionsManager registerController:[HomeSectionController class] forRoute:@"www/index.html"];

Calling JS from native code is also possible using the section bridge,
after you registered your handlers in JavaScript with ``axemas.register``::

    axemas.register("handler_name", function(data, callback) {
        callback({data: data});
    });

Calling ``handler_name`` from native code from a ``SectionController``
is possibile using the javascript bridge ``callHandler``::

    [self.section.bridge callHandler:@"handler_name" 
                                data:@{@"key": @"value"} 
                    responseCallback:^(id responseData) {
            NSLog(@"Callback with responseData: %@", responseData);
        }];

``SectionController`` available callbacks:

- *sectionDidLoad* triggered when the webpage finished loading
- *sectionWillLoad* just before the webpage will start to load

Android
-------

Inside ``sectionWillLoad`` method of ``SectionController`` subclass
it is possible to register handlers which will be available
in Javascript using ``axemas.call``::

    this.section.getJSBridge().registerHandler("openMap", new JavascriptBridge.Handler() {
        @Override
        public void call(Object data, JavascriptBridge.Callback callback) {

            String uri = "https://maps.google.com/maps";
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            section.startActivity(i);

        }
    });

Registering the ``SectionController`` for a section can be done
using the ``NavigationSectionsManager``::

    NavigationSectionsManager
                .registerController(this,HomeSectionController.class, "www/index.html");

Calling JS from native code is also possible using the section bridge,
after you registered your handlers in JavaScript with ``axemas.register``::

    axemas.register("handler_name", function(data, callback) {
        callback({data: data});
    });

Calling ``handler_name`` from native code from a ``SectionController``
is possibile using the javascript bridge ``callHandler``::

    this.section.getJSBridge().callJS("send-passenger-count", data, new JavascriptBridge.AndroidCallback() {
        @Override
        public void call(JSONObject data) {
            Log.d("axemas", "Callback with responseData: "+ data.toString());
        }
    });

``SectionController`` available callbacks:

- *sectionDidLoad* triggered when the webpage finished loading
- *sectionWillLoad* just before the webpage will start to load
- *sectionFragmentWillPause* triggered by fragment's onPause
- *sectionFragmentWillResume* triggered by fragment's onResume
- *fragmentOnActivityResult* triggered by fragment's onActivityResult

JavaScript API
==============

The JavaScript module ``axemas.js`` permits interaction with the native code of the application:

    - goto
    - gotoFromSidebar
    - call
    - alert
    - dialog
    - showProgressHUD
    - hideProgressHUD
    - getPlatform
    - storeData
    - fetchData
    - removeData

goto
----

Pushes new ``section`` on the navigation stack. It is the equivalent of the iOS ``[NavigationSectionsManager goto]`` and Android's ``NavigationSectionsManager.goTo()``.
All three functions accept a dictionary as **payload** which defines the extra actions the ``goto`` call must execute::

    axemas.goto(
        {"url":"www/home.html",
        "title":"HOME",
        "toggleSidebarIcon":"slide_icon",
        "stackMaintainedElements": 0,
        "stackPopElements": 0}
    );

The **payload** structure is shared between JavaScript, Objective C and Java, and accepts the following parameters:

    - ``url`` contains the local or remote address from which the WebView must load the content
    - ``title`` (optional) is the tile show in the application's ViewController / Action Bar.
    - ``toggleSidebarIcon`` (optional) is the sidebar's icon to be displayed and if missing a button to open the sidebar will not be created
    - ``stackMaintainedElements`` (optional) instructs the navigation stack to pop all views and maintain the last X ``sections`` indicated on the bottom of the stack; it is ill advised to use in conjunction with ``stackPopElements``
    - ``stackPopElements`` (optional) instructs the navigation stack to pop the first X ``sections``; it is ill advised to use in conjunction with ``stackMaintainedElements``

gotoFromSidebar
---------------

Same as ``goto`` but closes the sidebar and must be used only inside the sidebar ``section``. Refer to ``goto``::

    axemas.gotoFromSidebar(
        {"url":"www/home.html",
        "title":"HOME",
        "toggleSidebarIcon":"slide_icon",
        "stackMaintainedElements": 0,
        "stackPopElements": 0}
    );

call
----

The ``call`` enables JavaScript to execute a native registered handler inside a ``SectionController``::

    axemas.call('openNativeController');

    axemas.call('execute-and-return', '{"payload": "something"}', function(result) {
        alert(JSON.strgify(result));
    });

alert
-----

Creates a native dismissible alert dialog with a title and a message::

    axemas.alert('Alert title', "Alert message");


dialog
------

Generates a native dialog with a title, a message and a maximum of three buttons. When pressing a button a callback returns the button's value as integer, range [0-3]::

    axemas.dialog('Dialog title', 'Dialog display message', ['Cancel', 'Ok'],function(data) {
        axemas.alert('Pressed button', data.button);
    });

showProgressHUD
---------------

Locks interface interaction by displaying a spinner on the screen. The same spinner is always displayed when lading the contents of a page inside a ``section``::

     axemas.showProgressHUD();


hideProgressHUD
---------------

Used to dismiss a previously displayed progressHUD::

     axemas.hideProgressHUD();


getPlatform
-----------

Uses the ``navigator.userAgent`` object to determine if the current platform. Returns ``Android``, ``iOS`` or ``unsupported``::

     if (axemas.getPlatform() == 'your_platform') {
         //do something
     }


storeData
---------

Uses the WebView's ``localSotrage`` for key/value storing. Data stored will be available next time the application is launched::

    axemas.storeData("key","only_string_values");

fetchData
---------

Returns a previously stored ``value`` providing a ``key``::

    var value = axemas.fetchData("key");

removeData
----------

Permanently removes the previously saved data from the locationStorage::

    axemas.removeData("key")


Quick Project Setup
===================

An Axemas application is composed of a ``native code`` and ``html code``. Axema currently supports the ``Android`` and ``iOS`` platforms. The project found at ``repo.axant.it/axemas`` has the following directory structure:

- **axemas-android** contains the ``gradle/Android Studio`` based Android project
- **axemas-ios** contains  the iOS ``Xcode`` project
- **example** contains the ``www`` directory which is shared between the ``Android`` and ``iOS`` project and all shared code like html, js, css and resource files will be stored here
- **html** contains the Axemas JavaScript shared codebase and is linked inside the ``www`` directory as ``axemas``

We recommend splitting the project into three repositories containing the following:

- project-android having the following structure
    - ``axemas-android``
    - example (sub repository)
    - html
- project-ios
    - ``axemas-ios``
    - example (sub repository)
    - html
- project-www used as sub repository for the ``Android`` and ``iOS`` project

**Note:** you can always rename your project shared directories ``example`` and ``www``, but ensure to use relative links when changing the old ones inside the ``axemas-android`` and ``axemas-ios`` project directories.

Project renaming and package renaming should be done with the ``Xcode`` and ``Android Studio`` IDEs.

iOS extras
----------

Inside ``axemas-ios/axemas`` clone AXO  ``hg clone https://bitbucket.org/axant/axo``. Now you can run the project.
To rename the project just use Xcode's project renaming guide, it will even change your bundle's name.

Android extras
--------------

| No further intervention is needed for an Android project.
| 
| To rename the **package** open the ``AndroidManifest.xml`` with ``Android Studio`` and use ``Refractor -> Rename`` on the package name.
| 
| Rename the ``applicationId`` from ``it.axant.axemas`` under the ``/axemas-android/app/build.gradle`` to your new package name.
|
| To rename the **project** open the ``strings.xml`` under ``java/res/values`` and change ``app_name``.
|
| **Note:** verify the imports after a rename.