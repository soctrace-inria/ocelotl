# Ocelotl Analysis Tool

Ocelotl is an innovative visualization tool, which provides overviews for execution trace analysis by using a data aggregation technique. This technique enables to find anomalies in huge traces containing up to several billions of events, while keeping a fast computation time and providing a simple representation that does not overload the user.

Ocelotl is integrated into [Framesoc](http://soctrace-inria.github.io/framesoc/), a generic trace management and analysis infrastructure. You can take advantage of the tool bunch provided by Framesoc, and switch from an Ocelotl's overview to more detailed representations once you know where to focus.

![Ocelotl Screenshot](http://soctrace-inria.github.io/ocelotl/images/screenshots/ocelotl_spatiotemporal.png)

You will find here a video showing Ocelotl functionalities:

[![Ocelotl Analysis Tool](http://soctrace-inria.github.io/ocelotl/images/play.png)](https://www.youtube.com/watch?v=3cbLM0rUu78)

## Get Ocelotl Now!

### Update Site

This is the easiest way to start with Ocelotl.
First, [download](https://www.eclipse.org/) a version of Eclipse. We recommand [Eclipse Luna](http://www.eclipse.org/downloads/packages/release/Luna/SR2).
Then, install Ocelotl in Eclipse using the following procedure: 

- Go to _Help > Install New Software_. 
- Type the following address in the text field _Work with:_
  - http://soctrace-inria.github.io/updatesite/
- Select the feature(s) you want to install
- Press _Next_ and follow the wizard (accept the license and finish the installation).
- At the end of the installation, Eclipse will ask for restart.

### Quick Setup
- After the restart following a fresh installation, a configuration wizard will automatically pop up to perform the initial configuration.
- If no wizard is shown (because, for example, you already had a `soctrace.conf` configuration file in your Eclipse installation directory), do the following:
  - Launch the Framesoc perspective: _Windows > Open Perspective > Other... > Framesoc_
  - Launch the wizard: _Framesoc > Management > Initialize System_

### Java Heap Issue
In order to be able to use Ocelotl you might need to increase the maximum amount of memory available for the JVM, setting it to at least 4096 MB for best performance. To do this, you need to:
- Open the `eclipse.ini` file in the root directory of your eclipse installation
- Look for a line containing the -Xmx variable, like the following:

  `-Xmx####m` (#### is the current value of maximal memory in MB)

- Replace #### with at least 4096, in order to get the following line:

  `-Xmx4096m` 

### Download the Sources

Alternatively, you may want to work with Ocelotl sources.

[List of releases](https://github.com/soctrace-inria/ocelotl/releases)

Follow the [wiki](https://github.com/soctrace-inria/ocelotl/wiki/User-Guide) to set a developer environment.

## Wiki and User Guide

Check our [wiki](https://github.com/soctrace-inria/ocelotl/wiki/User-Guide) to install, configure and use Ocelotl.
A detailed [user guide](https://github.com/soctrace-inria/ocelotl/raw/master/docs/ocelotl_userguide.pdf) is also available for more details.

## Licence

Ocelotl is based on the Eclipse framework and it is released under the [EPL (Eclipse Public License) v1.0](https://www.eclipse.org/legal/epl-v10.html). The legal documentation has been written following the guidelines specified [here](http://www.eclipse.org/legal/guidetolegaldoc.php).

## Support and Contact
You can contact us by email:

[Damien Dosimont] (mailto:damien.dosimont-at-imag.fr),
[Youenn Corre] (mailto:youenn.corre-at-inria.fr),
[Generoso Pagano] (mailto:generoso.pagano-at-inria.fr) (replace -at- by @)

or follow us on [github](https://github.com/soctrace-inria/ocelotl).

### Ocelotl User List
You can subscribe to the Ocelotl user list using this [form](http://lists.gforge.inria.fr/cgi-bin/mailman/listinfo/ocelotl-users)
and post a message to the list members by sending an email to
[ocelotl-users-at-lists.gforge.inria.fr](mailto:ocelotl-users-at-lists.gforge.inria.fr)
