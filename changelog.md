Luciane66 — 11/18/2022 10:25 AM
alr lets check
alr seems to be working
but again, if possible, I'm trying to get rid of the intensity slider
it seems confusing for the user trying to decide the intensity when they themselves dont know what is playing and how loud it is
also, with this data, can you identify what notes are playing? like according to the amplitude or smth? like whats the key
𝐻𝒾𝓇𝑜 — 11/18/2022 3:12 PM
I'll work on the background color and slider first cos we're running out of time
then i review the note thing

// TODO change background color
when (intensity) {
    in 1..50 ->    {rgbRaw[0] = 255; rgbRaw[1] = 255; rgbRaw[2] = 255;} // white
    in 51..100 ->  {rgbRaw[0] = 255; rgbRaw[1] = 0  ; rgbRaw[2] = 0  ;} // red
    in 101..150 -> {rgbRaw[0] = 0  ; rgbRaw[1] = 255; rgbRaw[2] = 0  ;} // green
    in 151..200 -> {rgbRaw[0] = 0  ; rgbRaw[1] = 0  ; rgbRaw[2] = 255;} // blue
    in 201..255 -> {rgbRaw[0] = 130; rgbRaw[1] = 0  ; rgbRaw[2] = 255;} // pink
}

also n acho q vai dar tempo de fazer a formula pra achar as notas
https://stackoverflow.com/questions/32244435/how-to-get-each-frequency-of-recording-sound-using-mediarecorder

https://github.com/JorenSix/TarsosDSP

eu teria q refazer o projeto com esse outro recorder que pega a frequencia

Luciane66 — 11/20/2022 4:19 PM
I mean
The colours change too fast and kinda crazy but like for some reason it doesnt vibrate on my phone tbh
It did once kinda randomly
And I messed with the slider and everything
Also the slider and stuff are kinda hard to see so maybe like just add some background for those lol
And I mean maybe make the colour transition a bit slower

𝐻𝒾𝓇𝑜 — 11/20/2022 4:23 PM
fixed it
