# Mule CIFS transport #

This transport is similar to the file transport but allows accessing files on a remote Windows (Samba) server.

# Installation #
## Maven ##
This transport is deployed to the [MuleForge Maven repository](https://repository.mulesoft.org/releases/org/mule/transports/mule-transport-cifs). To use it from Maven, simply add the following repository definition to your pom.xml

    <repository>
        <id>mulesoft.releases</id>
        <name>Muleforge Releases Repository</name>
        <url>https://repository.mulesoft.org/releases/</url>
    </repository>

and delcare a dependency on the transport.

[Snapshots](https://repository.mulesoft.org/snapshots/org/mule/transports/mule-transport-cifs/) are available, too. If you want to use a snapshot, you need to add this repository declaration to your pom.xml:

    <repository>
        <id>mulesoft.releases</id>
        <name>Muleforge Releases Repository</name>
        <url>https://repository.mulesoft.org/snapshots/</url>
    </repository>

## Manual installation ##
To use this transport along with a Mule standalone distribution, simply download the transport jar from the [MuleForge Maven repository](https://repository.mulesoft.org/releases/org/mule/transports/mule-transport-cifs) and put it into `$MULE_HOME/lib/mule`. You also have to download the only depenency of this transport, cifs, from [Mule's third party repository](http://dist.codehaus.org/mule/dependencies/maven2/org/samba/jcifs/jcifs) and put it into `$MULE_HOME/lib/opt`.

Alternatively, you can bundle the two jar files along with your application.

# Usage #
The cifs transport is very similar to the [File transport](http://www.mulesoft.org/documentation/display/MULE3USER/File+Transport+Reference). It can be used to retrieve files from an SMB share:

    <flow name="smb2file">
        <smb:inbound-endpoint host="the-host" user="username" password="secret" path="/path"/>
        <file:outbound-endpoint path="/data"/>
    </flow>

or to store files on an SMB share:

    <flow name="file2smb">
        <file:inbound-endpoint path="/data"/>
        <smb:outbound-endpoint host="the-host" user="username" password="secret" path="/path"/>
    </flow>
