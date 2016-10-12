This is an experimental web server written using Java.
I was inspired to create this after having used a server
called "KeyFocus Web Server". I needed more functionality,
and didn't want to pay for something that I felt I could create,
so I started coding this around June 2014.
You may edit the source code and compile it, but please
give credit to the original author(br45entei@gmail.com).

Recommended startup script for compiled .jar(Windows):
	start "JavaWebServer" /B /WAIT java.exe -jar JavaWebServer.jar
(Of course you can also just double click it)
Starting the server without a console/command prompt will
cause the server to automatically create its own console window.
Default HTTP port is 80, SSL port is 443(disabled by default),
Administration port is 9727(enabled by default),
Proxy functions are built into the existing code and can be used
with both https and http.
HTTPS keystore/truststore features are untested as I have been unable
to test on a purchased domain name. Feedback on this area is especially
welcome.

============================================================================

Changelog:

Version 1.0_46: Changed version scheming to Major_Minor format

Version 1.0: Added project to GitHub
	Notes: The "JavaWebServer" and other classes as well are
		very large, as I need to work on organizing code into
		smaller appropriately named classes and other misc. fixes.
