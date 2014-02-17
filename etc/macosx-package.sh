#!/bin/sh

VERSION=$(find . -wholename ./target/mytunesrss-*-macosx/mytunesrss-*/MyTunesRSS.app | sed -e 's/.*\/mytunesrss-\(.*\)-macosx\/mytunesrss-.*/\1/')
APP=./target/mytunesrss-${VERSION}-macosx/mytunesrss-${VERSION}/MyTunesRSS.app

# signining and packaging
codesign -s "3rd Party Mac Developer Application: Michael Descher" -f --deep --signature-size 16384 $APP
productbuild --component $APP /Applications/ --sign "3rd Party Mac Developer Installer: Michael Descher" --product $APP/Contents/Info.plist ./target/MyTunesRSS-${VERSION}.pkg

# verification
spctl --assess --type execute $APP
spctl --assess --type install ./target/MyTunesRSS-${VERSION}.pkg