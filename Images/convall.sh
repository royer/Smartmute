#!/bin/bash

# res path of this android project, must be a absoluted path.

res_path_root=$PWD/../android/Smartmute/app/src/main/res



svg_to_png_py=$PWD"/svg2png.py"
recdir ()
{
	for file in *; do

		if [ -d "$file" ]; then
			pushd $PWD > /dev/null
			cd -- $file
			recdir
		fi
		
		ext="${file##*.}"
		if [ $ext = svg ]; then
			$svg_to_png_py $file $res_path_root
		fi	
	done

	popd > /dev/null
}

pushd $PWD > /dev/null
recdir
