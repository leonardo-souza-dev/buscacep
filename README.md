# Busca CEP com Mapa
Aplicativo Android para busca de CEP com mapa

### Características
* Busca de CEP informando:
 * tipo logradouro (rua, avendida, travessa, etc)
 * logradouro
 * Bairro
 * Cidade
 * UF
* Histório de CEP's pesquisados offline

### Tecnologias
* Android Maps v2
* Integração com duas API's para obtenção do endereço completo, uma utiliza Retrofit e outra AsyncTask;
* Intents e services
* Views: Activities, Fragments, Linear Layout;
* Internacionalização através de string resources (en-US, pt-BR);
* Persistência de dados no dispositivo com Shared Preferences;
* ArrayAdapter nativo para ListView
* Versão mínima: Android 4.0.3 KitKat (API 15)
* GSon, para desserializar o response da API
* AndroidMask, máscara de texto (https://github.com/jansenfelipe/androidmask)
