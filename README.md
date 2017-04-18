# xcrBungeePerms

Plugin służy do zarządzania permisjami graczy z poziomu BungeeCord'a zarówno na proxy jak i na serwerach Spigot, które są pod nim. Wszystkie permisje, grupy, zależności i gracze są zapisywani w bazie danych, w razie potrzeby ładowani do pamięci i synchronizowani między proxy, a instancjami serwerów.

## Co i jak?

Pluginy udostępniam, bo i tak leżą na dysku a w MC nie będę się już bawić. Wszystko działało z wersjami 1.7/1.8, jednak, gdy ktoś znajdzie jakiegoś buga niech zrobi zgłoszenie i dokładnie go opisze. Na wszelkie nowe funkcjonalności robicie Pull request'a, jak ktoś je sprawdzi to wtedy zostaną dodane. Wszystkie nowości zmieniające rozgrywkę najlepiej jak będą możliwe do wyłączenia w configu.

Z pluginów możecie korzystać w jakikolwiek sposób chcecie, kod możecie modyfikować do woli, możecie się z niego uczyć jak bardzo to Wam pomoże. Jedyne o co się uprasza to:
1. Nie podpisywanie się jako autor opublikowanych pluginów (nawet po drobnych modyfikacjach).
2. Wszystko co może w jakikolwiek sposób poprawić opublikowany projekt zgłaszajcie/poprawajcie etc.

## Opis pliku konfiguracyjnego

| Klucz | Wartość | Opis |
| ----- | ------- | ---- |
| mysql.host | tekst (string) | Adres IP serwera MySQL, z którym ma połączyć się plugin. Dla serwera MySQL postawionego na maszynie lokalnej wpisujemy `localhost` bądź `127.0.0.1`. |
| mysql.base | tekst (string) | Nazwa bazy danych, której będzie używał plugin. |
| mysql.user | tekst (string) | Nazwa użytkownika (z dostępem do ww. bazy). Jako ten użytkownik plugin będzie łączył się z bazą i wykonywał wszelakie operacje na danych. |
| mysql.pass | tekst (string) | Hasło do konta ww. użytkownika. |

Ze względu na bezpieczeństwo **stanowczo odradzam** wpisywania do configu danych konta **root** serwera MySQL.

#### Wygląd przykładowego pliku konfiguracyjnego

```yaml
config:
  mysql:
    host: "localhost"
    base: "xcr"
    user: "xcrafters"
    pass: "xcrafters123"
  redis:
    host: ""
```
Wartość klucza `redis.host` jest pusta, dlatego że w tym przykładzie nie korzystam z Redisa.

## Omówienie komend

| Komenda | Wymagane uprawnienie | Opis działania |
| ------- | -------------------- | -------------- |
| /perm user `nick` info | perms.manage | Wyświetla nick, przydzieloną grupę i całość uprawnień gracza o nicku `nick` - tych wynikających z przynależności do grupy, jak i dodanych mu osobno. |
| /perm user `nick` addperm `uprawnienie` | perms.manage | Dodaje pojedyncze uprawnienie `uprawnienie` graczowi o nicku `nick`
| /perm user `nick` remperm `uprawnienie` | perms.manage | Zabiera określone w argumencie `uprawnienie` uprawnienie graczowi o nicku `nick` które zostało mu dodane manualnie korzystając z powyższej komendy. |
| /perm user `nick` setperm `uprawnienie` true/false | perms.manage | Dla gracza o nicku `nick` ustawia, czy ma być przyznane uprawnienie `uprawnienie` Przydatne, gdy gracz ma uprawnienie wynikające z przynależności do jakiejś grupy, ale nie chcemy, by posiadał je osobiście - ustawiając mu przyznanie uprawnienia `uprawnienie` na false. |
| /perm user `nick` setgroup `grupa` | perms.manage | Ustawia graczowi o nicku `nick` przynależność do grupy `grupa` Użycie **default** jako argumentu `grupa` spowoduje ustawienie graczowi domyślnej grupy. |
||||
| /perm group `nazwa` info | perms.manage | Wyświetla nazwę i uprawnienia określonej grupy. |
| /perm group `nazwa` create | perms.manage | Tworzy nową grupę o podanej nazwie. |
| /perm group `nazwa` rename `nowa` | perms.manage | Zmienia nazwę istniejącej grupy z `nazwa` na `nowa` |
| /perm group `nazwa` remove | perms.manage | Usuwa istniejącą grupę o podanej nazwie. |
| /perm group `nazwa` addperm `uprawnienie` | perms.manage | Dodaje uprawnienie `uprawnienie` określonej grupie. |
| /perm group `nazwa` remperm `uprawnienie` | perms.manage | Usuwa dodane wcześniej uprawnienie `uprawnienie` określonej grupie. |
| /perm group `nazwa` setperm `uprawnienie` true/false | perms.manage | Ustawia, czy uprawnienie `uprawnienie` ma być negowane dla członków określonej grupy. Ustawienie **true** spowoduje przyznanie uprawnienia, natomiast **false** jego negację. |
| /perm group `nazwa` addinherit `grupa` | perms.manage | Ustawia podgrupę dla określonej grupy - "rozszerza" to uprawnienia grupy podstawowej o grupę rozszerzaną, czyli podgrupę. Zwięźlej mówiąc - ustawia grupie o nazwie `nazwa` dziedziczenie uprawnień z grupy `grupa` |
| /perm group `nazwa` reminherit `grupa` | perms.manage | Usuwa podgrupę o nazwie `grupa` grupie określonej w argumencie `nazwa` |

#### Przykładowe zastosowanie dziedziczenia uprawnień

Dla przykładu utworzymy dwie grupy - Helper oraz Moderator.
```
/perm group helper create
/perm group moderator create
```
Mając te grupy, dodajmy kilka uprawnień do obydwu.
```
/perm group helper addperm bany.czasowe
/perm group helper addperm bany.kickowanie

/perm group moderator addperm bany.permanentne
```
Jak widać w przykładzie, do grupy Helper dodałem uprawnienia do banowania czasowego i kickowania graczy z serwera (to tylko fikcyjne uprawnienia!), a do grupy Moderator - banowanie permanentne. Chcę teraz, by grupa Moderator miała **swoje uprawnienia + uprawnienia grupy Helper**. Oznacza to, że grupa Moderator po tym zabiegu będzie mogła banować permanentnie, czasowo i kickować z serwera. Do dzieła.
```
/perm group moderator addinherit helper
```
W taki oto sposób działa dziedziczenie uprawnień grup na serwerze.

## TODO
- [x] Napisanie README do pluginu (lista komend, jak używać configu, etc.)
- [ ] Tworzenie tabel w bazie danych przy pierwszym uruchomieniu
- [ ] Redis!!!
- [ ] Sensowne API
