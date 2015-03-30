#Ocelotl Analysis Tool

Ocelotl is an innovative visualization tool, which provides overviews for execution trace analysis by using a data aggregation technique. This technique enables to find anomalies in huge traces containing up to several billions of events, while keeping a fast computation time and providing a simple representation that does not overload the user.

Ocelotl is integrated into [Framesoc](http://soctrace-inria.github.io/framesoc/), a generic trace management and analysis infrastructure. You can take advantage of the tool bunch provided by Framesoc, and switch from an Ocelotl's overview to more detailed representations once you know where to focus.

![Ocelotl Screenshot](http://soctrace-inria.github.io/ocelotl/images/screenshots/ocelotl_spatiotemporal.png)

You will find here a video showing Ocelotl functionalities

[![Ocelotl Analysis Tool](data:image/png;base64,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)](https://www.youtube.com/watch?v=3cbLM0rUu78)

##Get Ocelotl Now!

### Update Site

This is the easiest way to start with Ocelotl.
First, [download](https://www.eclipse.org/) a version of Eclipse. We recommand [Eclipse Kepler](http://www.eclipse.org/downloads/packages/release/Kepler/SR2).
Then, install Ocelotl in Eclipse using the following procedure: Go to _Help > Install New Software_. In the new window, put the following URL in the field _Work with:_ 

http://soctrace-inria.github.io/updatesite/

and press Enter. Select all the available tools. Then click on _Next_, _Next_, accept the license agreement and click on _Finish_.

### Download the Sources

Alternatively, you may want to work with Ocelotl sources.

[List of releases](https://github.com/soctrace-inria/ocelotl/releases)

Follow the [wiki](https://github.com/soctrace-inria/ocelotl/wiki/User-Guide) to set a developer environment.

## Wiki and User Guide

Check our [wiki](https://github.com/soctrace-inria/ocelotl/wiki/User-Guide) to install, configure and use Ocelotl.
A detailed [user guide](https://github.com/soctrace-inria/ocelotl/raw/master/docs/OCELOTL_userguide.pdf) is also available for more details.

##Licence

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
