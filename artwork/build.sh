#!/bin/bash

# App icon:
for pair in mdpi:48 hdpi:72 xhdpi:96 xxhdpi:144 xxxhdpi:192; do
	DENSITY=${pair%:*}
	SIZE=${pair#*:} ; 
	FOLDER=../res/drawable-$DENSITY
	mkdir -p $FOLDER
	inkscape --export-png $FOLDER/ic_launcher.png -w $SIZE -h $SIZE icon.svg
done

inkscape --export-png ../icon.png -w 512 -h 512 icon.svg
