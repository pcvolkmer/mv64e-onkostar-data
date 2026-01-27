# Changelog

## [0.4.3](https://github.com/pcvolkmer/mv64e-onkostar-data/compare/v0.4.2...v0.4.3) (2026-01-27)


### Features

* do not filter incomplete data ([#115](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/115)) ([85709a1](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/85709a1b0d3ca6cbb96088666522be46c6bb78f3))
* do not filter missing position, altAllele and refAllele ([#111](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/111)) ([5806c22](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/5806c22addc0e641e9da896b0aabe64186e0f969))
* get simple variant data from form as default ([#114](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/114)) ([422172d](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/422172da6d28a68fd812bb694d5baa6e4843eb13))

## [0.4.2](https://github.com/pcvolkmer/mv64e-onkostar-data/compare/v0.4.1...v0.4.2) (2026-01-22)


### Features

* extract sequencing metadata from the corresponding property catalog as it can be documented in MolGen ([#110](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/110)) ([994478f](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/994478f0e58d1e53260fce698b807dff3d261773))
* map short protein change to required long form ([#109](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/109)) ([454e907](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/454e907d70beebb6c270c277e2c6d3067e70eb8f))
* map simple variants without (optional) end position ([#107](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/107)) ([180da19](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/180da199ac74643d651040c1e03ac8b7471f4ac2))

## [0.4.1](https://github.com/pcvolkmer/mv64e-onkostar-data/compare/v0.4.0...v0.4.1) (2026-01-09)


### Features

* RecommendationsMissing and NoSequencingPerformedReason ([3b76eca](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/3b76ecaf3cc347ed11ad736744ddc015a96040a6))
* RecommendationsMissing and NoSequencingPerformedReason ([#104](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/104)) ([3b76eca](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/3b76ecaf3cc347ed11ad736744ddc015a96040a6))

## [0.4.0](https://github.com/pcvolkmer/mv64e-onkostar-data/compare/v0.3.2...v0.4.0) (2026-01-02)


### ⚠ BREAKING CHANGES

* accept missing ecog and fail in DNPM:DIP ([#103](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/103))

### Features

* revert commit d35e850 due to DNPM:DIP changes ([#95](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/95)) ([29846ae](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/29846aedbd276b1f5d836c76ea52d4f6a39cff81))


### Bug Fixes

* accept missing ecog and fail in DNPM:DIP ([#103](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/103)) ([7132157](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/7132157ae0040645af862b42ba062c11f1421e64))
* return null if ecog date is missing ([#101](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/101)) ([8484a0f](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/8484a0f164eeeb01944cac710f818b474aba00bf))

## [0.3.2](https://github.com/pcvolkmer/mv64e-onkostar-data/compare/v0.3.1...v0.3.2) (2025-12-29)


### Features

* extract propcat entries from data catalogues ([#88](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/88)) ([77721d6](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/77721d6b811a2e1192034ae8f9e629c6bae72834))
* ignore invalid evidence grading ([#92](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/92)) ([3a4b232](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/3a4b2329be2d2546b69c55ba3abd1cb7714c320e))

## [0.3.1](https://github.com/pcvolkmer/mv64e-onkostar-data/compare/v0.3.0...v0.3.1) (2025-12-19)


### Features

* use "fresh-tissue" as sample-conservation in case "blood" ([#85](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/85)) ([1223d2e](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/1223d2eccce5a24a3a0a308e1c633afea8214e7f))


### Bug Fixes

* map "EudraCT" from PropCat as value for study system ([#86](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/86)) ([18e8d37](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/18e8d37e420436032767cbd03c7e5a5aad9405ae))

## [0.3.0](https://github.com/pcvolkmer/mv64e-onkostar-data/compare/v0.2.5...v0.3.0) (2025-12-15)


### ⚠ BREAKING CHANGES

* update dto lib to version 0.2.0 ([#83](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/83))

### deps

* update dto lib to version 0.2.0 ([#83](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/83)) ([9a63d67](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/9a63d6798a01f9afe2e350e0031040d207f02389))

## [0.2.5](https://github.com/pcvolkmer/mv64e-onkostar-data/compare/v0.2.4...v0.2.5) (2025-12-15)


### Features

* add try-catch chain and related methods ([#77](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/77)) ([a28989c](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/a28989cd73a31ea5d76526c7f9ab957a7945b7e7))
* add tuple result values for try-catch chain ([#79](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/79)) ([8e4681d](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/8e4681d54a7bc18a2454dd8a568bcf4174121b44))
* ignore diagnosis mapping errors from [#63](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/63) ([#80](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/80)) ([e0126bf](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/e0126bf5344ac327adef7eab6c9622c271806b66))
* try/catch for procedures and therapies ([#82](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/82)) ([00e0b47](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/00e0b479ae4514215b8c800590d7d5b243fd227c))

## [0.2.4](https://github.com/pcvolkmer/mv64e-onkostar-data/compare/v0.2.3...v0.2.4) (2025-12-10)


### Features

* add IgnorableMappingException and related method ([#75](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/75)) ([284c984](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/284c9848250f820d39f535b4b387a692852749f3))
* add study name as display to study references ([#69](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/69)) ([239a3ef](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/239a3ef829d2084e78c46a482031c76f38aaa8fa))

## [0.2.3](https://github.com/pcvolkmer/mv64e-onkostar-data/compare/v0.2.2...v0.2.3) (2025-12-09)


### Bug Fixes

* missing evidenzlevel ([#67](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/67)) ([6ae4e17](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/6ae4e170237c0db43243618fecbc5f2509f38b76))

## [0.2.2](https://github.com/pcvolkmer/mv64e-onkostar-data/compare/v0.2.1...v0.2.2) (2025-12-09)


### Features

* add further null checks and marks ([#62](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/62)) ([3fcab12](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/3fcab12e4e612e082933f24e4996229b1262a323))


### Bug Fixes

* bunch of potiential NPEs ([#66](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/66)) ([7b5d979](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/7b5d9798dfc9294b56d3c9072c5be0658dd6e714))
* multiple records for deleted KPA ([#65](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/65)) ([3a7e67f](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/3a7e67fd52729dabf8b6529ec431d16400de7bdc))
* TNM-T should not include null codes ([#59](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/59)) ([4b2d143](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/4b2d1438745c8795d407d3fe54229bce7284db21))

## [0.2.1](https://github.com/pcvolkmer/mv64e-onkostar-data/compare/v0.2.0...v0.2.1) (2025-12-05)


### Features

* add nullchecks in diagnosis mapper ([#56](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/56)) ([e388db4](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/e388db4c727e688edd86f30de7421f53c39f2859))
* sanitize values for TNM-T ([#58](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/58)) ([d35e850](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/d35e850fd922333ff1182a1d0e0393c8cedc2743))

## [0.2.0](https://github.com/pcvolkmer/mv64e-onkostar-data/compare/v0.1.1...v0.2.0) (2025-12-02)


### ⚠ BREAKING CHANGES

* use 'dev.pcvolkmer.mv64e' for package and group ([#52](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/52))

### Code Refactoring

* use 'dev.pcvolkmer.mv64e' for package and group ([#52](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/52)) ([b13a0d4](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/b13a0d485b7d8d8d57228ee076343afb64c1fa29))

## [0.1.1](https://github.com/pcvolkmer/mv64e-onkostar-data/compare/v0.1.0...v0.1.1) (2025-12-01)


### Features

* add "Stützende molekulare Alterationen" ([7a782ff](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/7a782ffc5487c65b42721bb5c4738a3914869658))
* add access to property catalogue ([a10842d](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/a10842df0c9cd285c0e0c7330ab1a039b7818a1b))
* add basic mapping for dk_dnpm_therapielinie ([6834bbd](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/6834bbd181e3c53c143ac65a4c9b38cf4a3b277b))
* add care plan notes ([3119921](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/3119921dddd33b887f7724a93ac5b7ee1521e66c))
* add catalogue for Immunhisto and PCR ([202fab9](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/202fab9b50e674074cb8a694fab59e043c12796a))
* add config option for tumor cell content method ([2d98d09](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/2d98d09b744659d18e2e28875e7c35b85dc030f1))
* add custom metadata request ([7a448c0](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/7a448c0f9156e1250a3e644044463da324a92a75))
* add display and system to diagnosis data ([6a3ee86](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/6a3ee860b75327ffe42b7c89414f18f2df89a004))
* add display and system to patient data ([21739a0](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/21739a055d23617b50b49a20be3182a851ea043c))
* add display and system to prozedur data ([673d89f](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/673d89f5e2c99f780910ba7aa98c580ae06c0413))
* add Evidenzlevel ([070ac3c](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/070ac3c2ab39adbd69fb7dcfc6731581c4af8486))
* add family member histories ([be1bbec](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/be1bbec44b521bf4fef6561fe7dde1222228f8f9))
* add genetic counseling recommendation ([798729b](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/798729b874973f9cfaeef28fd5dffd7511218405))
* add germline diagnosis codes ([ed1d6a9](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/ed1d6a9daba77fac39b044c185b1a0c087e2bbbb))
* add Histologie and related specimens ([4f82993](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/4f82993431aa7e8a4215c1551cfa91b01c3cdf28))
* add histologies of type sequencing to NGS reports ([#32](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/32)) ([3379121](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/33791211accad3983cc9c91a58ef983298a469a3))
* add initial support for recommendations ([1ab33de](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/1ab33de3f88415f949c4a5fc725a0f3744b8af31))
* add initial Therapieplan support ([a24855e](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/a24855ec7a4f6cfaf4b10bf6a6592c3a0564770b))
* add JSON medication mapper ([cab0fdd](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/cab0fdd3775a5d7fb933cb79c7e6c64d109a7631))
* add JSON to medication mapping for Einzelempfehlung ([4841aae](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/4841aae3e749cbe90d8c9c77f6c9b98554ada6bc))
* add mapping for KPA prozeduren ([01f8565](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/01f8565c612246827846f0b6076bcd8e08fad6ce))
* add medication to Therapielinie ([a249c29](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/a249c2930ada76781db2c60a648bb9f26465bcaf))
* add missing entries for therapies ([55ccf8c](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/55ccf8c87655606b2e89f529329774cfa7bb9845))
* add model project consent to mv metadata ([a6ba6c5](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/a6ba6c54e2b60d7fccbd5fb3a4668ee3bc67e06a))
* add MSI mapping as far as possible ([0984334](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/09843349d9596429d77cb62e8edd4917247ea4d7))
* add MTB episodes of care ([1e09495](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/1e09495d41f8e79f2fe789e955e8f1e0cd512dc4))
* add patient mapper ([f0ce22d](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/f0ce22da2f50e1a6587d8503900293d86895a261))
* add performance status mapping ([6f59bf7](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/6f59bf767a92f57a328ecbc4a32dc0e3572ab6bf))
* add performance status to Mtb data ([e848752](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/e848752e45ebba80290104968f8af9720e9fdba9))
* add project model consent ([fa24991](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/fa24991c9912b00180b435bb40ca21f058d00ed1))
* add recommendationsMissingReason and noSequencingPerformedReason ([111b96e](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/111b96ee50ee9b1748bf1965eca91abddc1e7065))
* add selection by patient id and tumor id ([3c9ecaa](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/3c9ecaa4d7ebf4fadcfa230eeddbd716be934f38))
* add specimens related to Vorbefunde ([37c553a](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/37c553a34e602261749330588bb4c222e693a61c))
* add subform catalogues for KPA ([690273f](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/690273f94ff459ab682103b9c363c9348a2dc52b))
* add therapielinie to Mtb data ([bd3b5ae](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/bd3b5aeda1dcf9419c0a0953cbcbb3255efce7e2))
* add tumor staging mapping ([4450c01](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/4450c01a17da96bf0ebd911363c9804bcc31f051))
* add Tumor-Proben ([60cbb0f](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/60cbb0ff8ac4160c215329bccd3c63561e09c326))
* add util method to create patient reference ([684c866](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/684c866b97625269bf91d5d4e32da0814e0aeccc))
* add version and display to Histologie ([f9e5e8c](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/f9e5e8c2eb700fc938a2b9e0df0c60bb3aa8b72d))
* add Vorbefunde mapper ([21ce6f3](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/21ce6f31853563d2f27e959c2ed04e99f6e0645d))
* additional mapping in TherapieplanDataMapper ([5692dfb](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/5692dfbdc7f48dc4744da5dd51799e70398a6152))
* case and patient id must not be null or blank ([#24](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/24)) ([146245f](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/146245f5e5a2f1a688d8a4e43d38b8680f48923d))
* clean utf8 strings ([#16](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/16)) ([294ba09](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/294ba0933d39a0eaac533adba353f5e528a38c26))
* common implementation for all subform data catalogues ([45303dc](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/45303dc8e02707893d52df9981639728198ce7b0))
* distinct non-null values in getByParentId ([#30](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/30)) ([b5693fd](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/b5693fd018ebcd25af1611fec1bd8dff4ef51c15))
* enhance health insurance export ([e9ef03f](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/e9ef03f791b04c223082d6fff0ce810c72a8bf86))
* extract mv consent from DNPM ConsentMVVerlauf ([48662e9](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/48662e9c6808d4a148d9c8ee5bfa8f6b150bb595))
* filter alterations references by related specimens ([0dba2c7](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/0dba2c7235d55b3f2ddbf331ee89dc93e9c4bbec))
* filter incomplete variants ([#42](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/42)) ([898ffb6](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/898ffb622f4f438804270fd9d072f1301c06fac6))
* further data mappings for NGS reports ([1c0558c](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/1c0558c29afa33fed1ccbb5d9cba777f914f8ff9))
* get date from care plan ([#35](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/35)) ([64d5587](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/64d5587a17a631140aa8fc8d970699713347edc8))
* get MolekulargenetikCatalogues by patient id ([#2](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/2)) ([76615dd](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/76615ddceaab2e36670a14514cc3143196a10d2f))
* initial support for NGS reports ([433dbbc](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/433dbbc18851a3f84f676e9f906122f7ba278e26))
* load both - patient and patient info from KPA ([e066fc4](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/e066fc437706e14d4ce12746a3f2b982e0e20066))
* make item filter optional ([feafc65](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/feafc655298c7f9296a817d2144ec75228a078c9))
* prefer ensemblid from form ([#40](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/40)) ([798ac77](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/798ac77ae77380d656d9e683bc5bbf74ff5a060c))
* require radio button checked to extract medication recommendations ([73526f2](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/73526f296e2b2861e324f786faab38b9248d4893))
* run closure if value is not null ([#29](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/29)) ([6d2bf67](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/6d2bf678422cbace0d892ab41bfbe61586694fc5))
* set to required value Bioinformatics by default ([8586b5d](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/8586b5dd396fe83ac2b7f786d5cb2ed16781d302))
* skip unknown vorbefunde ([#11](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/11)) ([f2f46a6](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/f2f46a6a2704baa62dc39f4665b42c7081c1b9e0))
* support for multiple choice fields ([87396e6](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/87396e692eb28195bd755d0e7a68694d31aeee78))
* use database id instead of einsendenummer ([3d52004](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/3d520042cbeaf3d0c143a962f351cf0a18087a07))
* use patient id, not patients database id ([3e5c2cb](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/3e5c2cbb80d1875afc8cbc2e265470468b05d032))
* use Tumorzellgehalt between 0.0 and 1.0 ([5346798](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/5346798caf25e1b001aa6c259bd420b42596c9a1))
* vorbefunde without reason(id) ([#14](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/14)) ([f3154eb](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/f3154eb48d6dca83521ddcb17089026b82ee9d98))


### Bug Fixes

* add some filters and cleanups ([94878fa](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/94878fa7fe930c379c59ac4df0f216bbb1f53aeb))
* ambiguous column id ([e8d4b36](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/e8d4b36d1351e6a681ab5bdcdf7cd2d3a01655d1))
* change column name to 'hauptprozedur_id' ([db49d89](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/db49d89f59561d4012f5fd49a4a0cb6a02f293c3))
* column name for therapy line ([e15457d](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/e15457d2af85bc2f755bac962afeb2430d117f79))
* database column names ([47449bb](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/47449bbaa367f0d5b3a6ae93c7c6011026b9f568))
* date alignment for different time zones ([bdf4bb0](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/bdf4bb0e23d4a1b59e73ccd8029205bc3b62c4ea))
* do not include empty tumor cell content value ([#48](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/48)) ([c210bca](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/c210bcaa931ad11ea642e4b0e21046ad538fb158))
* do not use empty therapy line value ([#9](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/9)) ([0b49218](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/0b492189b9f1eeefb9989c7b07e47b13ee8748b0))
* DOI publication id pattern ([f58022f](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/f58022f614805b92ec8e44e4e2274ab9bb067a19))
* filter procedures without valid dates ([#36](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/36)) ([a79bbe3](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/a79bbe361171d93bba6bdae4573f2f9b4a142aac))
* ignore refs if not set ([#8](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/8)) ([b91fffe](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/b91fffe5a0ff38ce6c2e21765c814c3b4819ad0d))
* issues in README.md status table ([bc11715](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/bc11715c4ffb043a6fd9dd495e9dc5f96ea1fc8c))
* missing sample date ([#44](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/44)) ([624e400](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/624e400527c73d4f13d33f689bba7a3dd597f5c0))
* NPE if evidenzlevel publication is null ([#33](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/33)) ([2b36df1](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/2b36df1930d69004477c06b9d5edca0186895abb))
* possible NPE if date is null in database ([#34](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/34)) ([5d8de4e](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/5d8de4e1651b170416e909b0e397bddad98200c0))
* possible npe if gender coding value not set ([#38](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/38)) ([5568566](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/5568566aca47ed1326ccc69f6c98aabe3f2c7bd3))
* possible NPE if no notes given ([c88f36f](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/c88f36f2ab08d923ae7bd66fe15ed5c2ca4b92f1))
* references to patients ([5121768](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/51217688aa7790a2f5b80d90e381c29d8622bf6c))
* return empty list if nothing available by parent id ([#13](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/13)) ([c6d8789](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/c6d8789df8ea783182e695eb87e0a089d98aedaf))
* usage of basedOn and reason ([1b93e85](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/1b93e852c51b1a27ef835932d3c79901d15d3140))
* use correct reason id ([#15](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/15)) ([afeeb79](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/afeeb792ddaef9fb5d5261e4f7518981556ca758))
* use onkostar provided commons-csv version ([#5](https://github.com/pcvolkmer/mv64e-onkostar-data/issues/5)) ([049acda](https://github.com/pcvolkmer/mv64e-onkostar-data/commit/049acda04d189579195b99a8fc8d1938745aa8dc))
