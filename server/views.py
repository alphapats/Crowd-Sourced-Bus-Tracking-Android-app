from django.http import HttpResponse

from django.http import JsonResponse
from math import radians, cos, sin, asin, sqrt
import time
import pickle
# Create your views here.
def distance(lon1, lat1, lon2, lat2):
    """
    Calculate the great circle distance between two points
    on the earth (specified in decimal degrees)
    """
    # convert decimal degrees to radians
    lon1, lat1, lon2, lat2 = map(radians, [lon1, lat1, lon2, lat2])
    # haversine formula
    dlon = lon2 - lon1
    dlat = lat2 - lat1
    a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlon/2)**2
    c = 2 * asin(sqrt(a))
    # Radius of earth in kilometers is 6371
    km = 6371* c
    met = km*1000
    #print(met)
    return met


def store(request,fname,data):
    #with open(fname, 'a+') as f:
    #    f.write(data+'\n')
    with open(fname, "rb") as myFile:
        gdata = pickle.load(myFile)
    #gdata = dict()
    l = data.split(',')
    dellist = []
    cur_time = round(time.time())
    for k,v in gdata.items():
        if(cur_time - v[2] > 10):
            dellist.append(k)
    for i in dellist:
        gdata.pop(i)
    gdata[l[0]] = [l[2],l[3],round(time.time())]
    #print(gdata)
    with open(fname, "wb") as myFile:
        pickle.dump(gdata, myFile)
    return HttpResponse("written to file."+data)

def returnLocation(request):
    #busLocation = []
    #busLocation.append({"Lat": 19.135250, "Long": 72.906744})
    #busLocation.append({"Lat": 19.135476, "Long": 72.910450})
    with open("location.csv", "rb") as myFile:
       gdata = pickle.load(myFile)
    busLocation = []
    tempLoc = []
    #busLocation.append({"Lat": 19.135250, "Long": 72.906744})
    cur_time = round(time.time())
    for k,v in gdata.items():
        if(cur_time - v[2] < 20):
            tempLoc.append([float(v[0]), float(v[1])])
    l = len(tempLoc)
    for i in range(l):
        val = True
        for j in range(i,l):
            if(i == j):
                continue
            if(distance(tempLoc[i][1], tempLoc[i][0], tempLoc[j][1], tempLoc[j][0]) < 4):
                val = False
        if(val == True):
            busLocation.append({"Lat": tempLoc[i][0], "Long": tempLoc[i][1]})

    response_data = {"busLocation": busLocation }
    # Bus_Location['2'] = '19.134520, 72.914271'
    # Bus_Location['4'] = '19.126005, 72.916123'
    return JsonResponse(response_data)