import sys
import codecs
import re
#import numpy as np

if __name__ == '__main__':
	f = codecs.open(sys.argv[1], 'r')
	html = f.read()    

	pkg_name = sys.argv[2]
	 
	lines = html.split("\n")
	search_keys = []
	reduced_lines = []

	for item in lines:
	    
	    words = item.split()

	    for w in words:
		if w[0:len(pkg_name)] == pkg_name: # here comes package_name...
		    parts = w.split(".")
		    if not parts[-1] in search_keys:
		        search_keys.append(parts[-1])

	# print search_keys
	classes_names = []    
	results = []
	for key in search_keys:
	    parts = key.split("</div>")
	    if parts != None:
		if len(parts) > 1 and (not "$" in parts[0]):
		    classes_names.append(parts[0])
		    
	#     print "==", key, "=="
	    for line  in  lines:
		if ('<div class="leaf">' in line) and key in line:
		    if ";"+key+"&" in line:
	#                 print line
		        results.append(line)

	i = 0 
	for line in results:
	    
	    if (" Activity.java" in line):
		continue
		
	    found = False
	    for _class in classes_names: 
		if _class in line:
		    found = True
	    if not found:
		continue
		
	    parts =  line.split("&nbsp;")
	    array = []	
	    for part in parts:
		if part != "":
		    array.append(part)
			 
	    #array = np.array(parts)
	    #mask = array!=""
	    #array = array[mask ]
	    print array[2],array[3],array[6],array[8]
#	    print "---------------------------------"
	    i += 1

