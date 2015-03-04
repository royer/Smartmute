#!/usr/bin/python

# vim: set tabstop=4 softtabstop=4 shiftwidth=4 :

# svg2pgn.py uses inkscape to convert svg file to png file
# 
# 

import os
import platform
import argparse
import subprocess
import ConfigParser

def gen_png(svg_file_path, dest_root_path,file_basename,\
		def_drawable_path,dpis):
	png_path = os.path.join(dest_root_path,def_drawable_path+dpis[0])
	if not os.path.exists(png_path):
		os.makedirs(png_path)
	png_file_path = os.path.join(png_path,file_basename+".png")
	inkscape_args = ["inkscape","-z"]
	inkscape_args.append("--export-width=" + str(dpis[1]))
	#inkscape_args.append("--export-height=" + str(dpis[1]))
	#inkscape_args.append("--export-dpi=" + str(dpis[1]))
	inkscape_args.append("--export-png="+png_file_path)
	inkscape_args.append(svg_file_path)
	result = subprocess.call(inkscape_args)
	if result != 0:
		print "Error on call inkscape when to generate "+png_path
		exit(1)


argParser = argparse.ArgumentParser(description='Generate all DPI png file' \
		' for android application')
argParser.add_argument("svgfile", help="the .svg file")
argParser.add_argument("respath", help='the root path of png files. usually' \
		' is "res" path of android project, default is current directory.', \
		nargs='?')
argParser.add_argument("--mdpi", help="the mdpi size. default is 32",\
		default=32,type=int)
argParser.add_argument("-s","--subpath",help="the subdirectory in the res path."\
		" default is drawable.",default="drawable")
argParser.add_argument("-d","--default-dpi",\
		help="the dpi for drawable path. usually is same as hdpi",
		choices=['mdpi','hdpi','xhdpi'],
		default='hdpi')
argParser.add_argument("-c","--config",\
		help='the config file of this svg file convert argument, if exist config'\
		' file, then the arguments in the config file overwrtie the command line ' \
		' arguments. if not provider this argument, svg2png also check the samename'\
		' with svg file with ".cfg" file in same directory of svg file',\
		nargs='?')
		
args = argParser.parse_args()

svg_file_path = os.path.abspath(args.svgfile)

mdpi = args.mdpi

def_drawable_path = args.subpath

def_dpi = args.default_dpi

if not os.path.exists(svg_file_path):
	print svg_file_path + " is not exist!"
	quit()

file_basename = os.path.splitext(os.path.basename(svg_file_path))[0]

if args.respath:
	dest_root_path = os.path.abspath(args.respath)
else:
	dest_root_path = os.path.abspath('.')

if (args.config):
	config_file = args.config
else:
	config_file = os.path.join(os.path.dirname(svg_file_path),file_basename+".cfg")
if os.path.exists(config_file):
	config = ConfigParser.ConfigParser(allow_no_value=True)
	config.read(config_file)
	t = config.get("svg2png","mdpi")
	if (t != None and t.isdigit()):
		mdpi = int(t)
	t = config.get("svg2png","subpath")
	if (t != None and len(t) > 0):
		def_drawable_path = t
	t = config.get("svg2png","default-dpi")
	if (t != None and \
			(t == "mdpi" or t == "hdpi" or t == "xhdpi")):
		def_dpi = t


dpis = []
dpis.append(["-mdpi",mdpi])
dpis.append(["-hdpi",int(mdpi*1.5)])
dpis.append(["-xhdpi",mdpi*2])
dpis.append(["-xxhdpi",mdpi*3])
dpis.append(["-xxxhdpi",mdpi*4])
if (def_dpi == "mdpi"):
	dpis.append(["",mdpi])
elif (def_dpi == "xhdpi"):
	dpis.append(["",mdpi*2])
else:
	dpis.append(["",int(mdpi*1.5)])

for dpi in dpis:
	gen_png(svg_file_path,dest_root_path,file_basename,def_drawable_path,\
		dpi)


