#Display Tag Updater 

--an andriod App for updating e-ink image on NFC-WISP 1.0
---
### Version
1.0


#Resource
---
###Tutorial & Discussion: 
See NFC-WISP wiki: http://nfc-wisp.wikispaces.com/

Please send your questions and suggestions to the [**discussion board**](http://nfc-wisp.wikispaces.com/wiki/messages) in wiki!!!	    

###Hardware Design repository:
https://github.com/wisp/nfc-wisp-hw.git

###Firmware repository:
Firmware for NFC-WISP 1.0 hardware 

https://github.com/wisp/nfc-wisp-fw.git		

#Introduction
---
1. This is an andriod app to update image of E-ink screen on NFC-WISP tag by tapping cell-phone onto it.
The app is tested on Nexus S, using CyanogenMod 11 OS (Andriod Version 4.4.4.). It only works with NFC-WISP 1.0 hareware and **nfc-eink-img-update-demo** firmware (see README.md in [firmware repo](https://github.com/wisp/nfc-wisp-fw.git        )). The older pre-release code is taged as **"pre-release"**.


#Configuration
---
1. Select **Improt project** from **File** in your andriod studio, and then navigate to the repo folder **/nfc-reader-app/** and select **andriod studio project**.
2. The build and load the **app** moudule, you will see an App named as **Display Tag Updater** in your cell-phone. 
3. The project is configured for Andriod OS 4.4 (API19) target, please modify it if you are using different target.

