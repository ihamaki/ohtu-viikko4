package ohtu.verkkokauppa;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class KauppaTest {

    Pankki pankki;
    Varasto varasto;
    Viitegeneraattori viiteg;
    Kauppa kauppa;

    @Before
    public void setUp() {
        pankki = mock(Pankki.class);
        varasto = mock(Varasto.class);
        viiteg = mock(Viitegeneraattori.class);
        kauppa = new Kauppa(varasto, pankki, viiteg);
    }

    @Test
    public void ostoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaan() {
        when(viiteg.uusi()).thenReturn(42);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("pekka", "12345");

        verify(pankki).tilisiirto(eq("pekka"), eq(42), eq("12345"), eq("33333-44455"), eq(5));
    }

    @Test
    public void ostetaanKaksiEriTuotettaJaLopuksiPankinMetodiaTilisiirtoKutsutaan() {
        when(viiteg.uusi()).thenReturn(42);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        when(varasto.saldo(2)).thenReturn(5);
        when(varasto.haeTuote(2)).thenReturn(new Tuote(1, "leipä", 3));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu("pekka", "12345");

        verify(pankki).tilisiirto(eq("pekka"), eq(42), eq("12345"), eq("33333-44455"), eq(8));
    }

    @Test
    public void ostetaanKaksiSamaaTuotettaJaLopuksiPankinMetodiaTilisiirtoKutsutaan() {
        when(viiteg.uusi()).thenReturn(42);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("martta", "12345");

        verify(pankki).tilisiirto(eq("martta"), eq(42), eq("12345"), eq("33333-44455"), eq(10));
    }

    @Test
    public void ostetaanLoppunutJaVarastossaOlevaTuoteJaLopuksiKutsutaanTilisiirtoa() {
        when(viiteg.uusi()).thenReturn(42);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        when(varasto.saldo(2)).thenReturn(0);
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "mehu", 6));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu("martta", "12345");

        verify(pankki).tilisiirto(eq("martta"), eq(42), eq("12345"), eq("33333-44455"), eq(5));
    }

    @Test
    public void aloitaAsiointiLuoUudenTyhjanOstoskorin() {
        when(viiteg.uusi()).thenReturn(42);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("martta", "10101");

        verify(pankki).tilisiirto(anyString(), anyInt(), anyString(), anyString(), eq(5));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("sinikka", "77777");
        verify(pankki).tilisiirto(anyString(), anyInt(), anyString(), anyString(), eq(10));
    }

    @Test
    public void pyydetaanUusiViiteJokaistaMaksuaVarten() {
        when(viiteg.uusi()).thenReturn(42);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("pekka", "12345");

        verify(viiteg, times(1)).uusi();

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("martta", "10101");

        verify(viiteg, times(2)).uusi();

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("sinikka", "77777");

        verify(viiteg, times(3)).uusi();
    }

    @Test
    public void poistoOstoskoristaToimiiOikein() {
        when(viiteg.uusi()).thenReturn(42);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        when(varasto.saldo(2)).thenReturn(5);
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "mehu", 6));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(2);
        kauppa.poistaKorista(1);
        kauppa.tilimaksu("martta", "10101");
        
        verify(pankki).tilisiirto(anyString(), anyInt(), anyString(), anyString(), eq(6));
    }

}
