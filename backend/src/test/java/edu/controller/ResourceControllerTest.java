package edu.controller;

import edu.configuration.FakeResourceLoaderConfiguration;
import edu.configuration.NoKafkaConfig;
import edu.configuration.SecurityConfig;
import edu.util.StatusCodeDescriptor;
import edu.web.ScrapperClient;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResourceController.class)
@Import({FakeResourceLoaderConfiguration.class, SecurityConfig.class, NoKafkaConfig.class})
class ResourceControllerTest {
    @MockBean
    private StatusCodeDescriptor statusCodeDescriptor;
    @MockBean
    private ScrapperClient scrapperClient;

    @Autowired
    private MockMvc mockMvc;

    private final List<String> listOfPngResources = List.of(
            "/icon_p.png",
            "/sign_up_icon.png",
            "/sign_up_icon_g.png",
            "/log_in_icon.png",
            "/log_in_icon_g.png",
            "/log_out_icon.png",
            "/log_out_icon_g.png",
            "/magnifier.png",
            "/clock_icon.png",
            "/created_icon.png",
            "/status_icon.png",
            "/views_icon.png",
            "/likes_icon.png",
            "/comments_icon.png",
            "/eye_closed.png",
            "/eye_open.png",
            "/standard_preview.png",
            "/standard_icon.png"
    );

    @SneakyThrows
    @Test
    void getIconSvg() {
        MockHttpServletResponse result = mockMvc.perform(get("/resources/icon.svg"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertThat(result.getContentType())
                .isEqualTo("image/svg+xml");
        assertThat(result.getHeader("Content-Disposition"))
                .isEqualTo("inline; filename=\"Icon.svg\"");
    }

    @SneakyThrows
    @Test
    void getIconPng() {
        for (String resource : listOfPngResources) {
            MockHttpServletResponse result =
                    mockMvc.perform(get("/resources" + resource))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse();

            assertThat(result.getContentType())
                    .isEqualTo(MediaType.IMAGE_PNG.toString());
            assertThat(result.getHeader("Content-Disposition"))
                    .isEqualTo("inline; filename=\"" +
                            resource
                                    .substring(1, 2)
                                    .toUpperCase() +
                                    resource.substring(2)
                            + "\""
                    );
        }
    }

    @SneakyThrows
    @Test
    void getPreviewImage() {
        String resource = "/preview/1";
        ResponseEntity<byte[]> expected = ResponseEntity.ok(
                "ÿØÿà  JFIF     ` `  ÿþ ;CREATOR: gd-jpeg v1.0 (using IJG JPEG v80), quality = 95 ÿÛ C                                                                 ÿÛ C                                                                 ÿÀ    ` `          ÿÄ                               ÿÄ µ                }        !1A  Qa \"q 2  ¡ #B±Á RÑð$3br        %&'()*456789:CDEFGHIJSTUVWXYZcdefghijstuvwxyz                 ¢£¤¥¦§¨©ª²³´µ¶·¸¹ºÂÃÄÅÆÇÈÉÊÒÓÔÕÖ×ØÙÚáâãäåæçèéêñòóôõö÷øùúÿÄ                               ÿÄ µ                w       !1  AQ aq \"2   B ¡±Á #3Rð brÑ  $4á%ñ    &'()*56789:CDEFGHIJSTUVWXYZcdefghijstuvwxyz                  ¢£¤¥¦§¨©ª²³´µ¶·¸¹ºÂÃÄÅÆÇÈÉÊÒÓÔÕÖ×ØÙÚâãäåæçèéêòóôõö÷øùúÿÚ          ? ýQÔµÈ¤  0  ÷K  ×'3©WÞjÈü Î ãî\u00ADN Sñ%åö  i   n   þYþ ±úÝlF!S£·è +J  4ô_© ªê¯{#\">#'j ô ~¼ôõ5ìÑ¨äÚ§è¾[³çñ2ºæ Oé §Õ 4%eQ!]¨G\"1Ýøô =OÒ½hRJ6 SÅ« wk©cÃÀ[ù ¼  UA ²¿U^¤ ö  ð¯S KÙÆíjq©ÞWè  jí5¡yÙ    ôoAë] -\u00ADQÕN¬]® SÄ Û ;¤  ÎzäÒjÇ|*=Ì <A ©\\\u00AD\u00AD   Wbcî   [=z lÔB\\òvèqU §;6_H   O4ÇÌ|¶öå «  AZê µ ªz.¿ ü9 ¬ë c ë Â   S× z ëPÝÎ¸EG  mÿ « qâÏ  ·7²±bÎÂ  ©Ïaþ> Õ4Þ õã gNÏ}ß èp ,ñÄ°BñÄè²È     ¤{d ÈS£ )z P ©?{úgØw7·Wl@l dªôükñ  ¯ ª©SZuWÙy³ß *4)óÉêÿ \u00AD Fæ÷ìvMq¼   } z  ÕëÆ§ÔðÞÑ;J[z UDëÔ³Z#&îð²  ±\" àõ=@üù¯O/ Jò÷ £ ÿ ËæxÙ hQ ²»{ ´ :)4ë  ·S>õ ·ÜèH ø#> õØUí%e«GËÔ ,\\ RÌÚ¼N¥c|ª ¡  ^Ô\"¢¬ 9VM#?QÕY×% Eû£5F´«$r·wòê7Â $Ü]È 0y© ÐïUW\"OVlé¡tõeu Q2ò³ýãý ¥Lc ½ ¤9 müÑ_WÖmì¢kÝ@à·ÝEä è* qv\\ìóï þ\"   ¸¸¸ ´« !ÏÜ Äç× õÀ¨´#¬ Ù  j×SKTy §â´»at®M¿+n åÀ< úg¿s Ë]<ª(÷á UÏ=ú  Í^Íuu~ú ê ]²© ×è8¬Ôb¢ OÌé¡ S>ã öO² üÎ$'v; Æ+ùÿ  ' * {ïæz «J« [lgêº  ûÛ @ Ç@ E ÛW í'i Ã.Èóý  íßúfT÷Ç [s9l  Ïs_A ªø n 4×þ þ ùÜ|¨Ñ  õ& ÅséÖÏ§=  IA ³ó  T{ 9¯¶ÂËê  4  \u00AD[ÚÍ·ÿ  S \\ PåO% `ª[g=? ]°ÅF:7¹Çì[ â z§ ¡Kf_0 ´ ÏAÓ rk\u00ADÔJ7º7§JMè`Ú_´SÇwn v l3 s X ô9¾[ ÑzÝ»z Lúý 6!Ó÷¡¤\" O20<±Ïlÿ *\"î®T ,o= wQÖ\u00ADâ MCX¼  wHGcÙ ~¿ä×=lD)«¶\\=¥w¢<£ÇZêø þ{ UÙ ±²Ãn  P8Q zuú×- õjÔR   A £   ]ä Á {ý¦R *  Àè  ~ ô þ´e9ûïEùù ¬\"êI ÷s\\© ¼ |Ü²1û ÷úUS j§$öØô¨Òº¿D}¥5ç »Há H =  8añNPç µ üM  òÅÿ ]LÙ'¸ B   ~R? t`ª9Ôö W×Eæqc`¡OÙ§ÑÝù ²k z-É Ë Ic]  ä7?{ · ú Y^  9GW××²î| =W¯5Íòÿ ?#2MuæRì0]²ÛxÉô ÕìF¿3ÕÝ <¡+7a {!  ÊÁ   n<g Jé !IZ/Ý[ù *Rºovd\\µÅìâ1©2³JNÄ   \\ ÔÖô14ë\u00ADb\u00ADsÓöq¤ì ¶ ÝN+6yAbbLG Î A ëïï^ éµÍ$  %Ò  sã)¬¤e0yK  %w Xõü1ïß&¸êâU5ÌÝßa<3\u00AD$ú   ªom ×37ÎÛÀ' :æ¼ ×  <¾_ð j  % \u00AD¿\u00ADN Å ¬Ö ¼Í9ûMÈÂ \\ bÏ@=Xñïü½ ½,J»V uõ~~ µG ´ ÙG¥é±Ïª6.dù¶nåXô P >¤ûUÎ\u00AD\\enZ  ×ò:h%: ô*Yé© #MpLhÇ÷ ~N E üþ û Ñ§ qÕÿ Z  3O  úÊîëÊ Ç    ëõ¯åzUa_ÝNÑ_ Èö+Ò© ÷ o'øz ×7¡T³ ` 8ï^½ D Téhº¿Ôñ*á½ïiU]ôF ¥w=Ì ÊÁ@  :/· M{ LU: ¢¥vÞÝÿ à#çqøY×©.M ß²ÿ 6Ìµ¼HAB«ÇRz o¥} ,S  v Ý 9V \"´ äÿ  ¹ÖÞ]¨îV4^ {û×b®ª_ y ¤ Iú AuöpÛ  ³`sÑ <~µéàå,4ù \u00AD\u00AD» ;Y\\ÌÔnÄaÝ% c9 8   õ®ê¸Õ*wè]*r NT·9ç [Ù É+   V?á^k¨¢ äîúÿ  ìÐ£+Ý¯DA©_ é§ÀI ÈÞz Ó¹?ÓÞ¢ 9Wn¬öÙ.þHõ©QäW[ÿ [ óCc%ÙÖîbD !  ×¨ ÿ Ý ©#µwJU ~¯ ¿Eç²õ  ¼cÉ  ¿s=Ò}v÷í3Ø T!£  Ëè[  9Çlã\u00AD{XzO KI´ßoÉ~WêtÓ :q²Öÿ Ó,% .æ+xß NÒ;ú v çÚ½ *¬aïµ©ÙN< »> »xÙð¯ rkù'ë # ¡²>Ò8*Ug 7½þå«v3.f2 ±±ÆxQü_ {TêþíAiÖOúè|ÔðÒ gQuv ëäejì<¿!$ã« õ÷¯S Û©}ßWÙv<\\} Ý(ìº/>þ Ï Ð°k  Õ  aÔöÇ\u00ADz 0Q  Õ--æy+(s  ×M¶ü¿à »,P ÙX¿Nq =~µÙG *µ ·èqUË] {}û .%wC4¤á¸U [ÿ \u00AD^µ n\"2zÙ?Äã  U4K_ËÔÄÕ$X í Dda 9È> ¥lO´J(ú  + wZK{ÿ *ïþ'Ó·Ì©hÒH  mä¶Ñÿ   é m(½ ¬m > ^âi½½:¶9mmí G (f;¤;s x Üô W\\«É;GWÓÈå \\ Þ óÿ  ædø ÓQºÔ-ô¨  y 4@ç é»Ø  r ç¥zùm*T¨<EKöõ¿o]¯ØÖ HT¼ Ëñ-5   °²Rå  ÌO,Äÿ  ùï^Æ  rU«;v^FÔ\"¥.iü ¥£h^j¬L¿»'20 ÈGaì)Ô  DÖý eÝù³zÕ müÏo/ø'¶X[Á «j÷Ñ   °DÇýk  ¯ãùViÆ ÉjüßCö¬&  3Ä5{û±¿H\u00ADåó2/Z;8 PrÇ  ëØWm MlMHÑ ·WÛúG  Ë°  4ñrZ½\" úþ²{ù ÒØ\"F²K f vÃÐ} Â½¥ Xj6½ ¶ï¯V|ÊË*c+óµ¤wí§Eýn  ©jßé Ü[g  ò ¥waa'F0[~w9+ÑU+Êo}½ ![ ¦ Ë;|År# Î;W¿ ¡ k £µºwò>{ BR\u00ADìà¯~¯§   < å¥ åW$ç  Õí¥ Ú[® }½O_ Ãó  ¸o4íþ ö ¯C Q¸¹ºRe%b  Un9îk×ÃZr Ý¿Á  ÙË/¡)EídÛêúEyõô ¦Í³÷*B¹ Ú   Æ½  cN>ê>s   J¼ÓwK ×ä 1ys;^ÄËåCÿ  í'Ý,x.}Oaúu\u00AD(Î v¨¯}_ íéúü  E: Q¤þ)n EÑz X,sü ªÆk >y  W=}¸ç â½jR *§µ«¥8|1óó:§   º%¯ùz÷.JmínSH  Ò ,¨   ´Ç\u00ADzðu1iOì\u00ADÛêM)}¾¿Ö ×Òm¥ ! .  T9<(î ?ð¯F4¢Ýþ ä\\  µÜô bÿ í     k¶(û*   ¯â Ý®ÙýI[ J å ¶ ¥ÑtHÎg c´>â ,0NÓ é£ t Q[«|    VÅQ j®Ö¼\u00ADé·ÜTÔggO!OQ cÑVºáVU«{Z ý ïèy?R§G ìi«u~^§ ªK O/ Èªx  àz×Ýå¾ÕRM\u00AD_V|fe Ãª¼  %×××±  Ä¶ÆéÛï F R ~=ÿ  {IF yßÅÓËÌòéeï  öIZ ·7 {E~¶+^È-\u00AD   +È7a¹ ¾§üÿ õóÃ§^|Ý þLßEçÝôGØÔ¥õZJ nKå -.ü EÕ Æ£©¶Iþ q   ÇÔÿ :ú|% ]÷ëþ^ ¡àãm[ 0OÊûù·ýæ Lw  BÑròÈ î  º½ >ýý«¦§½ Qü)Ù  àf (ái;+ö]ß EÓÌÔÔ k  Ã¶  Ò Þ?hÓ» tã =ýë,<^\"£«/á§¢ë'þG  ¥ì ëÔøßõ÷ ËªÙèpïFÝ °Ø:fG#  ýÉ OZö°Xg ¯ËßðF5©: ´ ¯_ø#${Í Lk ¼ cS9 3Ì)ê}0?Ï§ÖaiªÕ :kÜ þdCã´ ¿ Ïk  Ä!Ø  £;  ÁS ÿ g%²: c¹ãÐ  Ê¿ ¬)^§¼z · ^\\° \" ~ë Ö¿ 9T` Õ Ø ÂÒæç ¼  àÓ-q!ýã \"¯Wo@*©Âug¦Ëð9q|Ô¨Ù|RÙ [/3/U Î £ ¢ ¸VÂ : Þ½<%NJ©Óùwù#åñX û&«tÕÛD¼¯Ôãï Í?å±=w: /Ðw=«ô  êÉûêÝ ùö>   ¦¢ý ×«ý ä> {x@½  D aSüGü=Mi \u00ADR½wJ.ë¿õù ÆM C  UªFÍôëä¿Í ç uT¸f 6w¹2688è · WÓåØ.H. ü?à³ÅÌ12 %ëvú÷}?íØôîõ1 vu Ædr K º:f½¥ MZ;#ÂÄÕ   ï^¦»ÝÇ¥$ i® hu åf bO\\ Ïü CÃª  «ðöîí·¡òT)UÇVu&W ±é \\ ßòÖð| N ¨þ\" c×ß#Þ»)©V *VKKvò_ éË  8ë¢ßÔ H \\®»¬È~Ïd7[CÝÜ  ¹'ô ô¯¥Ãá i¨Cwñ>Ë±Ï  ü«} ù  èïuE¹Öî µËbv  #@2# =  >¦¾  F4i(@å 8Ó  ©yãK4Æ ¥ +÷@?~c× 0 w8®åN6æ <É»#×ã  xfc Î %½³_ÁR £©ý«R1 n;±  , êly»N ÿ Ë5ï J  £É »¹ç}Z ¥* ø Õö^WÙ Æ¥-Î® Õ ²¡ÊÆ:7¹õ'üû}   , =×ï¾½¼ ð8 k Ôæ ýÒÙwó}Û2M¨¸  x pÇÉÊc¿ uÏ¥{ ÿ u  »Éög%<;\u00AD7*±å z[ôü®fx UXm6  ÛåT_àZú,£ êK ì·ó}ÿ Èó³ o² \"Ýôì»  ÊÄÉ<æ[  PJ Ó>¦¾Â*I(Ajÿ 3ä % 97ëäK§Û c]U¦ çHUUxÙ ðH÷9Æ}øëÆø¬e<\"xZjòJÿ 7ßóòHòéeUó ûz   ì ó[v¼  ÎM%Ô þÊÙo ãPb æ  Ü mQ÷Tûö °æ  ëW yµVµû¾¶ò]Yè×ÁSÂRp¥ >Ïtº9yöD73Mx §v l²l \"2p; è8 S_M £N åO¥ÛíÙz½ý  ÙÚ íÖËÍõ /Í ÔR_µG 0Û    >nì~ éÐ« Òæ[_ï0«AÓ ¾×äUÔ5 t¸ qí àð  1 ñ5ìÐ µæõ  ìyÕf¯h\u00AD?7Ü ÃpÞê: ÙÀs,¸ó   §R9ÿ ?ZìSIY ü·gÓBÔD\u00AD6Ìúdò+øR¤ £Ì oÎ  e$eÞÜ-Ã½ K¸ õÒ0ãéïü«HQ (ª NÈù n!×¨ð  Ûø Ùyy × Ö°Ätû    îÆí »±à/ãÿ êô(á«Ô \u00ADV>ïÝ %Õ }zØzQö ¥ïë®öîßEó9]fìX[ \" O$ÌMº·Y ¼ ==  ¯¸ËpÑÄÔ÷í Çâ} H¯7×Ôø\\}w §ÍJó ïÈ ò}g/î®  u%ÍÜì÷²   UNqþ&¾þ   ¨(ÒV õ«>  +ÖÄ¹U å××² æÍ>ÌEÜÆ(F  z°  ·osJ wJJT×4Þ ]¼þ{ù#¢µ5Y{: å u ¯o ÞlÞÐ´}RõâÔîmR  5 u   EÎ ³Ôã$}w ÕáãªÑ    ¹\u00AD\u00ADI÷ Ý.Êú~ ©ô ,-Z³ &´yn ¥ åKfû»kå«ff½§ÙÉ®µ¼ ¬ Óå äíFûÅ #ænçð^8¯¥Ë¤©eñ©U|_ î¶^ ¢_6x  =¦>Téý ßg»õkvþH¤Èép/§¶hú%   BõÜß ã æ½j  Hªiß¬ wÙ~KÈójEÒ ©%n O¢ïú ú ¤÷×mk¥JÉ g3](Î{à{ _C ¥956¶Ùv<<Dág ú E£Ç4 ´Û×sî  ³  ñ¯f Ô ¬ó\\®ö/éFÔÞ. i Ø    $ }À}{ íÚ´R Ñ- iEÜú:ötX¼¹$Âõ`:±ì=   ]¹(£ûG R* ¾ôêrzÕÍî¡xÚ> É H Ún    sü ëÜó ú<.   ~Þ¾¯¢îü ëÐüû W  ÄÊ  Ñ Ú} ó}< Ü ê 2ÃO M¶Ý/ ÿ < Å9 ÄÇû£    uÐx Õ Iée§húy¾û       4ï.gªë+u [-,´^G#ãKÛ  Ãb¾eÔÀFe  älOoSßó¯²Èð´ã jÎÔã\u00AD»¾ïÌø|û UÍÓ¢¯Rv ~[YywîUÑ¼7  ¤Ë®êìw Ék ë$Øëô_çô®ÌÇ6 /  & {¯Y>Ñ_çù åÙ=,· <V%ûú¨-ï'×åù ´  MvÂóR² ¹`a¶uæVÆr º p ¹' \\x¼î  ö2µÕ¯Ùm§ ëäwåù ´ ]hÝßnï}{%²ómôH Ärßxhü·ks\u00ADj $ zB; Ïð¯¯ñ0 aqYåthæ/ Iª0Ö^} ó  ¿{  g:Ø ¹]ëOEåçè¿ nÆf áY +l b¬w3Kÿ -NrK Ôgæ? ¯   ·77/»¦ .ÚlxT2÷ÉÊ  ¿]w}õÜÍñõ  f  Øi.  'Æ6  ñîkßÈdë{Ò ¢ Vó<<î1¥îÆWo[ùz ¥ i ¹ X² 7  ð ®:WÜÓ Ü\u00AD ;  8Ç ·«%  ýûùp2ª°Ú²  ;ãÓ5Ò®×*z é;óu4ô©ã°D h_ 0 w O|÷5·4`  ü \\ ·?ÿÙ".getBytes()
        );
        when(scrapperClient.getImage(anyLong(), anyString()))
                .thenReturn(Mono.just(
                                expected
                        )
                );

        mockMvc.perform(get("/resources" + resource))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(expected))
                .andExpect(handler().method(
                        ResourceController.class.getMethod(
                                "getPreviewImage",
                                Long.class
                        )
                        )
                )
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void getUserIcon() {
        String resource = "/user_icon/1";
        ResponseEntity<byte[]> expected = ResponseEntity.ok(
                "ÿØÿà  JFIF     ` `  ÿþ ;CREATOR: gd-jpeg v1.0 (using IJG JPEG v80), quality = 95 ÿÛ C                                                                 ÿÛ C                                                                 ÿÀ    ` `          ÿÄ                               ÿÄ µ                }        !1A  Qa \"q 2  ¡ #B±Á RÑð$3br        %&'()*456789:CDEFGHIJSTUVWXYZcdefghijstuvwxyz                 ¢£¤¥¦§¨©ª²³´µ¶·¸¹ºÂÃÄÅÆÇÈÉÊÒÓÔÕÖ×ØÙÚáâãäåæçèéêñòóôõö÷øùúÿÄ                               ÿÄ µ                w       !1  AQ aq \"2   B ¡±Á #3Rð brÑ  $4á%ñ    &'()*56789:CDEFGHIJSTUVWXYZcdefghijstuvwxyz                  ¢£¤¥¦§¨©ª²³´µ¶·¸¹ºÂÃÄÅÆÇÈÉÊÒÓÔÕÖ×ØÙÚâãäåæçèéêòóôõö÷øùúÿÚ          ? ýQÔµÈ¤  0  ÷K  ×'3©WÞjÈü Î ãî\u00ADN Sñ%åö  i   n   þYþ ±úÝlF!S£·è +J  4ô_© ªê¯{#\">#'j ô ~¼ôõ5ìÑ¨äÚ§è¾[³çñ2ºæ Oé §Õ 4%eQ!]¨G\"1Ýøô =OÒ½hRJ6 SÅ« wk©cÃÀ[ù ¼  UA ²¿U^¤ ö  ð¯S KÙÆíjq©ÞWè  jí5¡yÙ    ôoAë] -\u00ADQÕN¬]® SÄ Û ;¤  ÎzäÒjÇ|*=Ì <A ©\\\u00AD\u00AD   Wbcî   [=z lÔB\\òvèqU §;6_H   O4ÇÌ|¶öå «  AZê µ ªz.¿ ü9 ¬ë c ë Â   S× z ëPÝÎ¸EG  mÿ « qâÏ  ·7²±bÎÂ  ©Ïaþ> Õ4Þ õã gNÏ}ß èp ,ñÄ°BñÄè²È     ¤{d ÈS£ )z P ©?{úgØw7·Wl@l dªôükñ  ¯ ª©SZuWÙy³ß *4)óÉêÿ \u00AD Fæ÷ìvMq¼   } z  ÕëÆ§ÔðÞÑ;J[z UDëÔ³Z#&îð²  ±\" àõ=@üù¯O/ Jò÷ £ ÿ ËæxÙ hQ ²»{ ´ :)4ë  ·S>õ ·ÜèH ø#> õØUí%e«GËÔ ,\\ RÌÚ¼N¥c|ª ¡  ^Ô\"¢¬ 9VM#?QÕY×% Eû£5F´«$r·wòê7Â $Ü]È 0y© ÐïUW\"OVlé¡tõeu Q2ò³ýãý ¥Lc ½ ¤9 müÑ_WÖmì¢kÝ@à·ÝEä è* qv\\ìóï þ\"   ¸¸¸ ´« !ÏÜ Äç× õÀ¨´#¬ Ù  j×SKTy §â´»at®M¿+n åÀ< úg¿s Ë]<ª(÷á UÏ=ú  Í^Íuu~ú ê ]²© ×è8¬Ôb¢ OÌé¡ S>ã öO² üÎ$'v; Æ+ùÿ  ' * {ïæz «J« [lgêº  ûÛ @ Ç@ E ÛW í'i Ã.Èóý  íßúfT÷Ç [s9l  Ïs_A ªø n 4×þ þ ùÜ|¨Ñ  õ& ÅséÖÏ§=  IA ³ó  T{ 9¯¶ÂËê  4  \u00AD[ÚÍ·ÿ  S \\ PåO% `ª[g=? ]°ÅF:7¹Çì[ â z§ ¡Kf_0 ´ ÏAÓ rk\u00ADÔJ7º7§JMè`Ú_´SÇwn v l3 s X ô9¾[ ÑzÝ»z Lúý 6!Ó÷¡¤\" O20<±Ïlÿ *\"î®T ,o= wQÖ\u00ADâ MCX¼  wHGcÙ ~¿ä×=lD)«¶\\=¥w¢<£ÇZêø þ{ UÙ ±²Ãn  P8Q zuú×- õjÔR   A £   ]ä Á {ý¦R *  Àè  ~ ô þ´e9ûïEùù ¬\"êI ÷s\\© ¼ |Ü²1û ÷úUS j§$öØô¨Òº¿D}¥5ç »Há H =  8añNPç µ üM  òÅÿ ]LÙ'¸ B   ~R? t`ª9Ôö W×Eæqc`¡OÙ§ÑÝù ²k z-É Ë Ic]  ä7?{ · ú Y^  9GW××²î| =W¯5Íòÿ ?#2MuæRì0]²ÛxÉô ÕìF¿3ÕÝ <¡+7a {!  ÊÁ   n<g Jé !IZ/Ý[ù *Rºovd\\µÅìâ1©2³JNÄ   \\ ÔÖô14ë\u00ADb\u00ADsÓöq¤ì ¶ ÝN+6yAbbLG Î A ëïï^ éµÍ$  %Ò  sã)¬¤e0yK  %w Xõü1ïß&¸êâU5ÌÝßa<3\u00AD$ú   ªom ×37ÎÛÀ' :æ¼ ×  <¾_ð j  % \u00AD¿\u00ADN Å ¬Ö ¼Í9ûMÈÂ \\ bÏ@=Xñïü½ ½,J»V uõ~~ µG ´ ÙG¥é±Ïª6.dù¶nåXô P >¤ûUÎ\u00AD\\enZ  ×ò:h%: ô*Yé© #MpLhÇ÷ ~N E üþ û Ñ§ qÕÿ Z  3O  úÊîëÊ Ç    ëõ¯åzUa_ÝNÑ_ Èö+Ò© ÷ o'øz ×7¡T³ ` 8ï^½ D Téhº¿Ôñ*á½ïiU]ôF ¥w=Ì ÊÁ@  :/· M{ LU: ¢¥vÞÝÿ à#çqøY×©.M ß²ÿ 6Ìµ¼HAB«ÇRz o¥} ,S  v Ý 9V \"´ äÿ  ¹ÖÞ]¨îV4^ {û×b®ª_ y ¤ Iú AuöpÛ  ³`sÑ <~µéàå,4ù \u00AD\u00AD» ;Y\\ÌÔnÄaÝ% c9 8   õ®ê¸Õ*wè]*r NT·9ç [Ù É+   V?á^k¨¢ äîúÿ  ìÐ£+Ý¯DA©_ é§ÀI ÈÞz Ó¹?ÓÞ¢ 9Wn¬öÙ.þHõ©QäW[ÿ [ óCc%ÙÖîbD !  ×¨ ÿ Ý ©#µwJU ~¯ ¿Eç²õ  ¼cÉ  ¿s=Ò}v÷í3Ø T!£  Ëè[  9Çlã\u00AD{XzO KI´ßoÉ~WêtÓ :q²Öÿ Ó,% .æ+xß NÒ;ú v çÚ½ *¬aïµ©ÙN< »> »xÙð¯ rkù'ë # ¡²>Ò8*Ug 7½þå«v3.f2 ±±ÆxQü_ {TêþíAiÖOúè|ÔðÒ gQuv ëäejì<¿!$ã« õ÷¯S Û©}ßWÙv<\\} Ý(ìº/>þ Ï Ð°k  Õ  aÔöÇ\u00ADz 0Q  Õ--æy+(s  ×M¶ü¿à »,P ÙX¿Nq =~µÙG *µ ·èqUË] {}û .%wC4¤á¸U [ÿ \u00AD^µ n\"2zÙ?Äã  U4K_ËÔÄÕ$X í Dda 9È> ¥lO´J(ú  + wZK{ÿ *ïþ'Ó·Ì©hÒH  mä¶Ñÿ   é m(½ ¬m > ^âi½½:¶9mmí G (f;¤;s x Üô W\\«É;GWÓÈå \\ Þ óÿ  ædø ÓQºÔ-ô¨  y 4@ç é»Ø  r ç¥zùm*T¨<EKöõ¿o]¯ØÖ HT¼ Ëñ-5   °²Rå  ÌO,Äÿ  ùï^Æ  rU«;v^FÔ\"¥.iü ¥£h^j¬L¿»'20 ÈGaì)Ô  DÖý eÝù³zÕ müÏo/ø'¶X[Á «j÷Ñ   °DÇýk  ¯ãùViÆ ÉjüßCö¬&  3Ä5{û±¿H\u00ADåó2/Z;8 PrÇ  ëØWm MlMHÑ ·WÛúG  Ë°  4ñrZ½\" úþ²{ù ÒØ\"F²K f vÃÐ} Â½¥ Xj6½ ¶ï¯V|ÊË*c+óµ¤wí§Eýn  ©jßé Ü[g  ò ¥waa'F0[~w9+ÑU+Êo}½ ![ ¦ Ë;|År# Î;W¿ ¡ k £µºwò>{ BR\u00ADìà¯~¯§   < å¥ åW$ç  Õí¥ Ú[® }½O_ Ãó  ¸o4íþ ö ¯C Q¸¹ºRe%b  Un9îk×ÃZr Ý¿Á  ÙË/¡)EídÛêúEyõô ¦Í³÷*B¹ Ú   Æ½  cN>ê>s   J¼ÓwK ×ä 1ys;^ÄËåCÿ  í'Ý,x.}Oaúu\u00AD(Î v¨¯}_ íéúü  E: Q¤þ)n EÑz X,sü ªÆk >y  W=}¸ç â½jR *§µ«¥8|1óó:§   º%¯ùz÷.JmínSH  Ò ,¨   ´Ç\u00ADzðu1iOì\u00ADÛêM)}¾¿Ö ×Òm¥ ! .  T9<(î ?ð¯F4¢Ýþ ä\\  µÜô bÿ í     k¶(û*   ¯â Ý®ÙýI[ J å ¶ ¥ÑtHÎg c´>â ,0NÓ é£ t Q[«|    VÅQ j®Ö¼\u00ADé·ÜTÔggO!OQ cÑVºáVU«{Z ý ïèy?R§G ìi«u~^§ ªK O/ Èªx  àz×Ýå¾ÕRM\u00AD_V|fe Ãª¼  %×××±  Ä¶ÆéÛï F R ~=ÿ  {IF yßÅÓËÌòéeï  öIZ ·7 {E~¶+^È-\u00AD   +È7a¹ ¾§üÿ õóÃ§^|Ý þLßEçÝôGØÔ¥õZJ nKå -.ü EÕ Æ£©¶Iþ q   ÇÔÿ :ú|% ]÷ëþ^ ¡àãm[ 0OÊûù·ýæ Lw  BÑròÈ î  º½ >ýý«¦§½ Qü)Ù  àf (ái;+ö]ß EÓÌÔÔ k  Ã¶  Ò Þ?hÓ» tã =ýë,<^\"£«/á§¢ë'þG  ¥ì ëÔøßõ÷ ËªÙèpïFÝ °Ø:fG#  ýÉ OZö°Xg ¯ËßðF5©: ´ ¯_ø#${Í Lk ¼ cS9 3Ì)ê}0?Ï§ÖaiªÕ :kÜ þdCã´ ¿ Ïk  Ä!Ø  £;  ÁS ÿ g%²: c¹ãÐ  Ê¿ ¬)^§¼z · ^\\° \" ~ë Ö¿ 9T` Õ Ø ÂÒæç ¼  àÓ-q!ýã \"¯Wo@*©Âug¦Ëð9q|Ô¨Ù|RÙ [/3/U Î £ ¢ ¸VÂ : Þ½<%NJ©Óùwù#åñX û&«tÕÛD¼¯Ôãï Í?å±=w: /Ðw=«ô  êÉûêÝ ùö>   ¦¢ý ×«ý ä> {x@½  D aSüGü=Mi \u00ADR½wJ.ë¿õù ÆM C  UªFÍôëä¿Í ç uT¸f 6w¹2688è · WÓåØ.H. ü?à³ÅÌ12 %ëvú÷}?íØôîõ1 vu Ædr K º:f½¥ MZ;#ÂÄÕ   ï^¦»ÝÇ¥$ i® hu åf bO\\ Ïü CÃª  «ðöîí·¡òT)UÇVu&W ±é \\ ßòÖð| N ¨þ\" c×ß#Þ»)©V *VKKvò_ éË  8ë¢ßÔ H \\®»¬È~Ïd7[CÝÜ  ¹'ô ô¯¥Ãá i¨Cwñ>Ë±Ï  ü«} ù  èïuE¹Öî µËbv  #@2# =  >¦¾  F4i(@å 8Ó  ©yãK4Æ ¥ +÷@?~c× 0 w8®åN6æ <É»#×ã  xfc Î %½³_ÁR £©ý«R1 n;±  , êly»N ÿ Ë5ï J  £É »¹ç}Z ¥* ø Õö^WÙ Æ¥-Î® Õ ²¡ÊÆ:7¹õ'üû}   , =×ï¾½¼ ð8 k Ôæ ýÒÙwó}Û2M¨¸  x pÇÉÊc¿ uÏ¥{ ÿ u  »Éög%<;\u00AD7*±å z[ôü®fx UXm6  ÛåT_àZú,£ êK ì·ó}ÿ Èó³ o² \"Ýôì»  ÊÄÉ<æ[  PJ Ó>¦¾Â*I(Ajÿ 3ä % 97ëäK§Û c]U¦ çHUUxÙ ðH÷9Æ}øëÆø¬e<\"xZjòJÿ 7ßóòHòéeUó ûz   ì ó[v¼  ÎM%Ô þÊÙo ãPb æ  Ü mQ÷Tûö °æ  ëW yµVµû¾¶ò]Yè×ÁSÂRp¥ >Ïtº9yöD73Mx §v l²l \"2p; è8 S_M £N åO¥ÛíÙz½ý  ÙÚ íÖËÍõ /Í ÔR_µG 0Û    >nì~ éÐ« Òæ[_ï0«AÓ ¾×äUÔ5 t¸ qí àð  1 ñ5ìÐ µæõ  ìyÕf¯h\u00AD?7Ü ÃpÞê: ÙÀs,¸ó   §R9ÿ ?ZìSIY ü·gÓBÔD\u00AD6Ìúdò+øR¤ £Ì oÎ  e$eÞÜ-Ã½ K¸ õÒ0ãéïü«HQ (ª NÈù n!×¨ð  Ûø Ùyy × Ö°Ätû    îÆí »±à/ãÿ êô(á«Ô \u00ADV>ïÝ %Õ }zØzQö ¥ïë®öîßEó9]fìX[ \" O$ÌMº·Y ¼ ==  ¯¸ËpÑÄÔ÷í Çâ} H¯7×Ôø\\}w §ÍJó ïÈ ò}g/î®  u%ÍÜì÷²   UNqþ&¾þ   ¨(ÒV õ«>  +ÖÄ¹U å××² æÍ>ÌEÜÆ(F  z°  ·osJ wJJT×4Þ ]¼þ{ù#¢µ5Y{: å u ¯o ÞlÞÐ´}RõâÔîmR  5 u   EÎ ³Ôã$}w ÕáãªÑ    ¹\u00AD\u00ADI÷ Ý.Êú~ ©ô ,-Z³ &´yn ¥ åKfû»kå«ff½§ÙÉ®µ¼ ¬ Óå äíFûÅ #ænçð^8¯¥Ë¤©eñ©U|_ î¶^ ¢_6x  =¦>Téý ßg»õkvþH¤Èép/§¶hú%   BõÜß ã æ½j  Hªiß¬ wÙ~KÈójEÒ ©%n O¢ïú ú ¤÷×mk¥JÉ g3](Î{à{ _C ¥956¶Ùv<<Dág ú E£Ç4 ´Û×sî  ³  ñ¯f Ô ¬ó\\®ö/éFÔÞ. i Ø    $ }À}{ íÚ´R Ñ- iEÜú:ötX¼¹$Âõ`:±ì=   ]¹(£ûG R* ¾ôêrzÕÍî¡xÚ> É H Ún    sü ëÜó ú<.   ~Þ¾¯¢îü ëÐüû W  ÄÊ  Ñ Ú} ó}< Ü ê 2ÃO M¶Ý/ ÿ < Å9 ÄÇû£    uÐx Õ Iée§húy¾û       4ï.gªë+u [-,´^G#ãKÛ  Ãb¾eÔÀFe  älOoSßó¯²Èð´ã jÎÔã\u00AD»¾ïÌø|û UÍÓ¢¯Rv ~[YywîUÑ¼7  ¤Ë®êìw Ék ë$Øëô_çô®ÌÇ6 /  & {¯Y>Ñ_çù åÙ=,· <V%ûú¨-ï'×åù ´  MvÂóR² ¹`a¶uæVÆr º p ¹' \\x¼î  ö2µÕ¯Ùm§ ëäwåù ´ ]hÝßnï}{%²ómôH Ärßxhü·ks\u00ADj $ zB; Ïð¯¯ñ0 aqYåthæ/ Iª0Ö^} ó  ¿{  g:Ø ¹]ëOEåçè¿ nÆf áY +l b¬w3Kÿ -NrK Ôgæ? ¯   ·77/»¦ .ÚlxT2÷ÉÊ  ¿]w}õÜÍñõ  f  Øi.  'Æ6  ñîkßÈdë{Ò ¢ Vó<<î1¥îÆWo[ùz ¥ i ¹ X² 7  ð ®:WÜÓ Ü\u00AD ;  8Ç ·«%  ýûùp2ª°Ú²  ;ãÓ5Ò®×*z é;óu4ô©ã°D h_ 0 w O|÷5·4`  ü \\ ·?ÿÙ".getBytes()
        );
        when(scrapperClient.getImage(anyLong(), anyString()))
                .thenReturn(Mono.just(
                                expected
                        )
                );

        mockMvc.perform(get("/resources" + resource))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(expected))
                .andExpect(handler().method(
                                ResourceController.class.getMethod(
                                        "getUserIcon",
                                        Long.class
                                )
                        )
                )
                .andDo(print());
    }

    @SneakyThrows
    @Test
    void getArticleIcon() {
        String resource = "/article/1/images/1";
        ResponseEntity<byte[]> expected = ResponseEntity.ok(
                "ÿØÿà  JFIF     ` `  ÿþ ;CREATOR: gd-jpeg v1.0 (using IJG JPEG v80), quality = 95 ÿÛ C                                                                 ÿÛ C                                                                 ÿÀ    ` `          ÿÄ                               ÿÄ µ                }        !1A  Qa \"q 2  ¡ #B±Á RÑð$3br        %&'()*456789:CDEFGHIJSTUVWXYZcdefghijstuvwxyz                 ¢£¤¥¦§¨©ª²³´µ¶·¸¹ºÂÃÄÅÆÇÈÉÊÒÓÔÕÖ×ØÙÚáâãäåæçèéêñòóôõö÷øùúÿÄ                               ÿÄ µ                w       !1  AQ aq \"2   B ¡±Á #3Rð brÑ  $4á%ñ    &'()*56789:CDEFGHIJSTUVWXYZcdefghijstuvwxyz                  ¢£¤¥¦§¨©ª²³´µ¶·¸¹ºÂÃÄÅÆÇÈÉÊÒÓÔÕÖ×ØÙÚâãäåæçèéêòóôõö÷øùúÿÚ          ? ýQÔµÈ¤  0  ÷K  ×'3©WÞjÈü Î ãî\u00ADN Sñ%åö  i   n   þYþ ±úÝlF!S£·è +J  4ô_© ªê¯{#\">#'j ô ~¼ôõ5ìÑ¨äÚ§è¾[³çñ2ºæ Oé §Õ 4%eQ!]¨G\"1Ýøô =OÒ½hRJ6 SÅ« wk©cÃÀ[ù ¼  UA ²¿U^¤ ö  ð¯S KÙÆíjq©ÞWè  jí5¡yÙ    ôoAë] -\u00ADQÕN¬]® SÄ Û ;¤  ÎzäÒjÇ|*=Ì <A ©\\\u00AD\u00AD   Wbcî   [=z lÔB\\òvèqU §;6_H   O4ÇÌ|¶öå «  AZê µ ªz.¿ ü9 ¬ë c ë Â   S× z ëPÝÎ¸EG  mÿ « qâÏ  ·7²±bÎÂ  ©Ïaþ> Õ4Þ õã gNÏ}ß èp ,ñÄ°BñÄè²È     ¤{d ÈS£ )z P ©?{úgØw7·Wl@l dªôükñ  ¯ ª©SZuWÙy³ß *4)óÉêÿ \u00AD Fæ÷ìvMq¼   } z  ÕëÆ§ÔðÞÑ;J[z UDëÔ³Z#&îð²  ±\" àõ=@üù¯O/ Jò÷ £ ÿ ËæxÙ hQ ²»{ ´ :)4ë  ·S>õ ·ÜèH ø#> õØUí%e«GËÔ ,\\ RÌÚ¼N¥c|ª ¡  ^Ô\"¢¬ 9VM#?QÕY×% Eû£5F´«$r·wòê7Â $Ü]È 0y© ÐïUW\"OVlé¡tõeu Q2ò³ýãý ¥Lc ½ ¤9 müÑ_WÖmì¢kÝ@à·ÝEä è* qv\\ìóï þ\"   ¸¸¸ ´« !ÏÜ Äç× õÀ¨´#¬ Ù  j×SKTy §â´»at®M¿+n åÀ< úg¿s Ë]<ª(÷á UÏ=ú  Í^Íuu~ú ê ]²© ×è8¬Ôb¢ OÌé¡ S>ã öO² üÎ$'v; Æ+ùÿ  ' * {ïæz «J« [lgêº  ûÛ @ Ç@ E ÛW í'i Ã.Èóý  íßúfT÷Ç [s9l  Ïs_A ªø n 4×þ þ ùÜ|¨Ñ  õ& ÅséÖÏ§=  IA ³ó  T{ 9¯¶ÂËê  4  \u00AD[ÚÍ·ÿ  S \\ PåO% `ª[g=? ]°ÅF:7¹Çì[ â z§ ¡Kf_0 ´ ÏAÓ rk\u00ADÔJ7º7§JMè`Ú_´SÇwn v l3 s X ô9¾[ ÑzÝ»z Lúý 6!Ó÷¡¤\" O20<±Ïlÿ *\"î®T ,o= wQÖ\u00ADâ MCX¼  wHGcÙ ~¿ä×=lD)«¶\\=¥w¢<£ÇZêø þ{ UÙ ±²Ãn  P8Q zuú×- õjÔR   A £   ]ä Á {ý¦R *  Àè  ~ ô þ´e9ûïEùù ¬\"êI ÷s\\© ¼ |Ü²1û ÷úUS j§$öØô¨Òº¿D}¥5ç »Há H =  8añNPç µ üM  òÅÿ ]LÙ'¸ B   ~R? t`ª9Ôö W×Eæqc`¡OÙ§ÑÝù ²k z-É Ë Ic]  ä7?{ · ú Y^  9GW××²î| =W¯5Íòÿ ?#2MuæRì0]²ÛxÉô ÕìF¿3ÕÝ <¡+7a {!  ÊÁ   n<g Jé !IZ/Ý[ù *Rºovd\\µÅìâ1©2³JNÄ   \\ ÔÖô14ë\u00ADb\u00ADsÓöq¤ì ¶ ÝN+6yAbbLG Î A ëïï^ éµÍ$  %Ò  sã)¬¤e0yK  %w Xõü1ïß&¸êâU5ÌÝßa<3\u00AD$ú   ªom ×37ÎÛÀ' :æ¼ ×  <¾_ð j  % \u00AD¿\u00ADN Å ¬Ö ¼Í9ûMÈÂ \\ bÏ@=Xñïü½ ½,J»V uõ~~ µG ´ ÙG¥é±Ïª6.dù¶nåXô P >¤ûUÎ\u00AD\\enZ  ×ò:h%: ô*Yé© #MpLhÇ÷ ~N E üþ û Ñ§ qÕÿ Z  3O  úÊîëÊ Ç    ëõ¯åzUa_ÝNÑ_ Èö+Ò© ÷ o'øz ×7¡T³ ` 8ï^½ D Téhº¿Ôñ*á½ïiU]ôF ¥w=Ì ÊÁ@  :/· M{ LU: ¢¥vÞÝÿ à#çqøY×©.M ß²ÿ 6Ìµ¼HAB«ÇRz o¥} ,S  v Ý 9V \"´ äÿ  ¹ÖÞ]¨îV4^ {û×b®ª_ y ¤ Iú AuöpÛ  ³`sÑ <~µéàå,4ù \u00AD\u00AD» ;Y\\ÌÔnÄaÝ% c9 8   õ®ê¸Õ*wè]*r NT·9ç [Ù É+   V?á^k¨¢ äîúÿ  ìÐ£+Ý¯DA©_ é§ÀI ÈÞz Ó¹?ÓÞ¢ 9Wn¬öÙ.þHõ©QäW[ÿ [ óCc%ÙÖîbD !  ×¨ ÿ Ý ©#µwJU ~¯ ¿Eç²õ  ¼cÉ  ¿s=Ò}v÷í3Ø T!£  Ëè[  9Çlã\u00AD{XzO KI´ßoÉ~WêtÓ :q²Öÿ Ó,% .æ+xß NÒ;ú v çÚ½ *¬aïµ©ÙN< »> »xÙð¯ rkù'ë # ¡²>Ò8*Ug 7½þå«v3.f2 ±±ÆxQü_ {TêþíAiÖOúè|ÔðÒ gQuv ëäejì<¿!$ã« õ÷¯S Û©}ßWÙv<\\} Ý(ìº/>þ Ï Ð°k  Õ  aÔöÇ\u00ADz 0Q  Õ--æy+(s  ×M¶ü¿à »,P ÙX¿Nq =~µÙG *µ ·èqUË] {}û .%wC4¤á¸U [ÿ \u00AD^µ n\"2zÙ?Äã  U4K_ËÔÄÕ$X í Dda 9È> ¥lO´J(ú  + wZK{ÿ *ïþ'Ó·Ì©hÒH  mä¶Ñÿ   é m(½ ¬m > ^âi½½:¶9mmí G (f;¤;s x Üô W\\«É;GWÓÈå \\ Þ óÿ  ædø ÓQºÔ-ô¨  y 4@ç é»Ø  r ç¥zùm*T¨<EKöõ¿o]¯ØÖ HT¼ Ëñ-5   °²Rå  ÌO,Äÿ  ùï^Æ  rU«;v^FÔ\"¥.iü ¥£h^j¬L¿»'20 ÈGaì)Ô  DÖý eÝù³zÕ müÏo/ø'¶X[Á «j÷Ñ   °DÇýk  ¯ãùViÆ ÉjüßCö¬&  3Ä5{û±¿H\u00ADåó2/Z;8 PrÇ  ëØWm MlMHÑ ·WÛúG  Ë°  4ñrZ½\" úþ²{ù ÒØ\"F²K f vÃÐ} Â½¥ Xj6½ ¶ï¯V|ÊË*c+óµ¤wí§Eýn  ©jßé Ü[g  ò ¥waa'F0[~w9+ÑU+Êo}½ ![ ¦ Ë;|År# Î;W¿ ¡ k £µºwò>{ BR\u00ADìà¯~¯§   < å¥ åW$ç  Õí¥ Ú[® }½O_ Ãó  ¸o4íþ ö ¯C Q¸¹ºRe%b  Un9îk×ÃZr Ý¿Á  ÙË/¡)EídÛêúEyõô ¦Í³÷*B¹ Ú   Æ½  cN>ê>s   J¼ÓwK ×ä 1ys;^ÄËåCÿ  í'Ý,x.}Oaúu\u00AD(Î v¨¯}_ íéúü  E: Q¤þ)n EÑz X,sü ªÆk >y  W=}¸ç â½jR *§µ«¥8|1óó:§   º%¯ùz÷.JmínSH  Ò ,¨   ´Ç\u00ADzðu1iOì\u00ADÛêM)}¾¿Ö ×Òm¥ ! .  T9<(î ?ð¯F4¢Ýþ ä\\  µÜô bÿ í     k¶(û*   ¯â Ý®ÙýI[ J å ¶ ¥ÑtHÎg c´>â ,0NÓ é£ t Q[«|    VÅQ j®Ö¼\u00ADé·ÜTÔggO!OQ cÑVºáVU«{Z ý ïèy?R§G ìi«u~^§ ªK O/ Èªx  àz×Ýå¾ÕRM\u00AD_V|fe Ãª¼  %×××±  Ä¶ÆéÛï F R ~=ÿ  {IF yßÅÓËÌòéeï  öIZ ·7 {E~¶+^È-\u00AD   +È7a¹ ¾§üÿ õóÃ§^|Ý þLßEçÝôGØÔ¥õZJ nKå -.ü EÕ Æ£©¶Iþ q   ÇÔÿ :ú|% ]÷ëþ^ ¡àãm[ 0OÊûù·ýæ Lw  BÑròÈ î  º½ >ýý«¦§½ Qü)Ù  àf (ái;+ö]ß EÓÌÔÔ k  Ã¶  Ò Þ?hÓ» tã =ýë,<^\"£«/á§¢ë'þG  ¥ì ëÔøßõ÷ ËªÙèpïFÝ °Ø:fG#  ýÉ OZö°Xg ¯ËßðF5©: ´ ¯_ø#${Í Lk ¼ cS9 3Ì)ê}0?Ï§ÖaiªÕ :kÜ þdCã´ ¿ Ïk  Ä!Ø  £;  ÁS ÿ g%²: c¹ãÐ  Ê¿ ¬)^§¼z · ^\\° \" ~ë Ö¿ 9T` Õ Ø ÂÒæç ¼  àÓ-q!ýã \"¯Wo@*©Âug¦Ëð9q|Ô¨Ù|RÙ [/3/U Î £ ¢ ¸VÂ : Þ½<%NJ©Óùwù#åñX û&«tÕÛD¼¯Ôãï Í?å±=w: /Ðw=«ô  êÉûêÝ ùö>   ¦¢ý ×«ý ä> {x@½  D aSüGü=Mi \u00ADR½wJ.ë¿õù ÆM C  UªFÍôëä¿Í ç uT¸f 6w¹2688è · WÓåØ.H. ü?à³ÅÌ12 %ëvú÷}?íØôîõ1 vu Ædr K º:f½¥ MZ;#ÂÄÕ   ï^¦»ÝÇ¥$ i® hu åf bO\\ Ïü CÃª  «ðöîí·¡òT)UÇVu&W ±é \\ ßòÖð| N ¨þ\" c×ß#Þ»)©V *VKKvò_ éË  8ë¢ßÔ H \\®»¬È~Ïd7[CÝÜ  ¹'ô ô¯¥Ãá i¨Cwñ>Ë±Ï  ü«} ù  èïuE¹Öî µËbv  #@2# =  >¦¾  F4i(@å 8Ó  ©yãK4Æ ¥ +÷@?~c× 0 w8®åN6æ <É»#×ã  xfc Î %½³_ÁR £©ý«R1 n;±  , êly»N ÿ Ë5ï J  £É »¹ç}Z ¥* ø Õö^WÙ Æ¥-Î® Õ ²¡ÊÆ:7¹õ'üû}   , =×ï¾½¼ ð8 k Ôæ ýÒÙwó}Û2M¨¸  x pÇÉÊc¿ uÏ¥{ ÿ u  »Éög%<;\u00AD7*±å z[ôü®fx UXm6  ÛåT_àZú,£ êK ì·ó}ÿ Èó³ o² \"Ýôì»  ÊÄÉ<æ[  PJ Ó>¦¾Â*I(Ajÿ 3ä % 97ëäK§Û c]U¦ çHUUxÙ ðH÷9Æ}øëÆø¬e<\"xZjòJÿ 7ßóòHòéeUó ûz   ì ó[v¼  ÎM%Ô þÊÙo ãPb æ  Ü mQ÷Tûö °æ  ëW yµVµû¾¶ò]Yè×ÁSÂRp¥ >Ïtº9yöD73Mx §v l²l \"2p; è8 S_M £N åO¥ÛíÙz½ý  ÙÚ íÖËÍõ /Í ÔR_µG 0Û    >nì~ éÐ« Òæ[_ï0«AÓ ¾×äUÔ5 t¸ qí àð  1 ñ5ìÐ µæõ  ìyÕf¯h\u00AD?7Ü ÃpÞê: ÙÀs,¸ó   §R9ÿ ?ZìSIY ü·gÓBÔD\u00AD6Ìúdò+øR¤ £Ì oÎ  e$eÞÜ-Ã½ K¸ õÒ0ãéïü«HQ (ª NÈù n!×¨ð  Ûø Ùyy × Ö°Ätû    îÆí »±à/ãÿ êô(á«Ô \u00ADV>ïÝ %Õ }zØzQö ¥ïë®öîßEó9]fìX[ \" O$ÌMº·Y ¼ ==  ¯¸ËpÑÄÔ÷í Çâ} H¯7×Ôø\\}w §ÍJó ïÈ ò}g/î®  u%ÍÜì÷²   UNqþ&¾þ   ¨(ÒV õ«>  +ÖÄ¹U å××² æÍ>ÌEÜÆ(F  z°  ·osJ wJJT×4Þ ]¼þ{ù#¢µ5Y{: å u ¯o ÞlÞÐ´}RõâÔîmR  5 u   EÎ ³Ôã$}w ÕáãªÑ    ¹\u00AD\u00ADI÷ Ý.Êú~ ©ô ,-Z³ &´yn ¥ åKfû»kå«ff½§ÙÉ®µ¼ ¬ Óå äíFûÅ #ænçð^8¯¥Ë¤©eñ©U|_ î¶^ ¢_6x  =¦>Téý ßg»õkvþH¤Èép/§¶hú%   BõÜß ã æ½j  Hªiß¬ wÙ~KÈójEÒ ©%n O¢ïú ú ¤÷×mk¥JÉ g3](Î{à{ _C ¥956¶Ùv<<Dág ú E£Ç4 ´Û×sî  ³  ñ¯f Ô ¬ó\\®ö/éFÔÞ. i Ø    $ }À}{ íÚ´R Ñ- iEÜú:ötX¼¹$Âõ`:±ì=   ]¹(£ûG R* ¾ôêrzÕÍî¡xÚ> É H Ún    sü ëÜó ú<.   ~Þ¾¯¢îü ëÐüû W  ÄÊ  Ñ Ú} ó}< Ü ê 2ÃO M¶Ý/ ÿ < Å9 ÄÇû£    uÐx Õ Iée§húy¾û       4ï.gªë+u [-,´^G#ãKÛ  Ãb¾eÔÀFe  älOoSßó¯²Èð´ã jÎÔã\u00AD»¾ïÌø|û UÍÓ¢¯Rv ~[YywîUÑ¼7  ¤Ë®êìw Ék ë$Øëô_çô®ÌÇ6 /  & {¯Y>Ñ_çù åÙ=,· <V%ûú¨-ï'×åù ´  MvÂóR² ¹`a¶uæVÆr º p ¹' \\x¼î  ö2µÕ¯Ùm§ ëäwåù ´ ]hÝßnï}{%²ómôH Ärßxhü·ks\u00ADj $ zB; Ïð¯¯ñ0 aqYåthæ/ Iª0Ö^} ó  ¿{  g:Ø ¹]ëOEåçè¿ nÆf áY +l b¬w3Kÿ -NrK Ôgæ? ¯   ·77/»¦ .ÚlxT2÷ÉÊ  ¿]w}õÜÍñõ  f  Øi.  'Æ6  ñîkßÈdë{Ò ¢ Vó<<î1¥îÆWo[ùz ¥ i ¹ X² 7  ð ®:WÜÓ Ü\u00AD ;  8Ç ·«%  ýûùp2ª°Ú²  ;ãÓ5Ò®×*z é;óu4ô©ã°D h_ 0 w O|÷5·4`  ü \\ ·?ÿÙ".getBytes()
        );
        when(scrapperClient.getImage(anyLong(), anyString()))
                .thenReturn(Mono.just(
                                expected
                        )
                );

        mockMvc.perform(get("/resources" + resource))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(expected))
                .andExpect(handler().method(
                                ResourceController.class.getMethod(
                                        "getArticleIcon",
                                        Long.class,
                                        Long.class
                                )
                        )
                )
                .andDo(print());
    }
}