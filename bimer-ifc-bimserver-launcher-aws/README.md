# BIMer IFC BIM Server Launcher AWS

This module's purpose is to have BIM Server launcher that can be installed on AWS Lambda Layer. The AWS Lambda Layer has a file size limit
and full `bimer-ifc-bimserver-launcher.jar` was too large to install it on AWS. This module declares a single dependency to 
`bimer-ifc-bimserver-launcher` and thanks to `maven-shade-plugin` its final JAR includes the same dependencies and resources as 
`bimer-ifc-bimserver-launcher`, but excluding unused `IfcGeomServer-*.zip` binaries. It makes the final JAR size small enough to install
it on AWS Lambda Layer. The final package contains also unpacked `IfcGeomServer-*-linux64.zip` binary. The reason is that AWS Lambda Layer
file system is read only and application cannot extract binary itself. It means that the final package contains both zipped and extracted
binaries. It looks bad, but works for now and changing it would require either changing AWS setup (if possible) or application logic.

The final ZIP (bimer-ifc-bimserver-launcher-aws-<version>>-bin.zip) is ready to use Lambda Layer, so it can be uploaded and referenced in
lambda.

After the ZIP is uploaded and installed as AWS Lambda Layer, then it is unpacked to virtual file system in `/opt` directory, so from AWS
Lambda point of view there is the following files structure:
- `/opt`
  - `/bimerServer`
    - `bimer-ifc-bimserver-launcher-aws-<version>.jar`
    - `IfcGeomServer` (binary file)
