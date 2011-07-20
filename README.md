# Mule CIFS transport #

This transport is similar to the file transport but allows accessing files on a remote Windows (Samba) server.

# Usage #
## Maven ##
This transport is deployed to the MuleForge Maven repository. To use it from Maven, simply decare the repository and add a dependency

## Manual usage ##
To use this transport along with a Mule standalone distribution, simply download the transport jar from <url here> and put it into $MULE_HOME/lib/mule. You also have to download the only depenency of this transport, cifs, from <url here> and put it into $MULE_HOME/lib/opt.

Alternatively, you can bundle the two jar files along with your application.