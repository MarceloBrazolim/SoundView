ok so i've made some mods and now the formula is like this
decibel = 50 * log10(strength / 1.736)
so if the ampli is like 15k or 50k the max db is gonna be like 255 so not to worr
also i've modified the text on the screen to show amplitude and intensity, since it's not even dbu with this formula anymore
i'll keep it like this for now so u at least have smt to show
i've alread got an idea on how to do the sensitivity limitation together w the amplitude multiplier
and i removed the slider for now since it doesnt do anything
also put a limiter before the hardware request so even if the decibel var gets higher than 255 it doesnt crash the app

```kt
decibel = (20 * log10(/*amp*/strength.toDouble() / /*amp_ref*/ 1.736)) * /*sensibility*/ 3.2
                    if (decibel.toInt() > 255) decibel = 255.0
                    if (decibel.toInt() < 0) decibel = 1.0
```

assim 15k ampli da a vibraçao maxima
se achar q ta mt sensivel avisa q eu abaixo
ja sei
pq n colocar a sensibilidade no slider?
coloco o valor entre 1 e 5 do multiplicador
e pronto

i actually went ahead and made just it
aumentei a frequencia da checagem

multiplicador de sensibilidade aplica no slider
[img/1.png] [img/2.png]