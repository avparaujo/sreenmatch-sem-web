package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner sc = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=7acbe567";

    public void exibeMenu(){
        System.out.println("Digite o nome da serie: ");
        String nomeSerie = sc.nextLine();
        nomeSerie = URLEncoder.encode(nomeSerie, StandardCharsets.UTF_8);

        var json = consumo.obterDados(ENDERECO + nomeSerie + API_KEY);
        DadosSerie serie = conversor.obterDados(json, DadosSerie.class);
        System.out.println(serie);

        List<DadosTemporada> temporadas = new ArrayList<>();
        for (int i = 1; i <= serie.totalTemporadas() ; i++) {
            json = consumo.obterDados(ENDERECO + nomeSerie + "&season=" + i + API_KEY);
            temporadas.add(conversor.obterDados(json, DadosTemporada.class));
        }

        List<Episodio> episodios = temporadas
                .parallelStream()
                .flatMap(t -> t.episodios().stream()
                    .map(d -> new Episodio(t.numero(), d)))
                .collect(Collectors.toList());
        episodios.forEach(System.out::println);

        Map<Integer, Double> avaliacoesTemporada = episodios.parallelStream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));
        avaliacoesTemporada.forEach((a, b) -> System.out.println("Temporada: %d - Avaliação: %.1f".formatted(a, b)));

        DoubleSummaryStatistics est = episodios.parallelStream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println("Média geral: "+ est.getAverage());
        System.out.println("Melhor avaliação: " + est.getMax());
        System.out.println("Pior avaliação: " + est.getMin());
        System.out.println("Total de avaliacoes: " + est.getCount());

        System.out.println("Digite o episodio do serie: ");
        var nomeEpisodio = sc.nextLine();

        Optional<Episodio> episodio = episodios.parallelStream()
                .filter(e -> e.getTitulo().toUpperCase().contains(nomeEpisodio.toUpperCase()))
                .findAny();

        if (episodio.isPresent()){
            System.out.println("""
                    Episodio encontrado!
                    Temporada: %d - Episodio: %d - %s - Avaliação: %.1f
                    """.formatted(episodio.get().getTemporada(),
                    episodio.get().getNumero(),
                    episodio.get().getTitulo(),
                    episodio.get().getAvaliacao()));
        } else {
            System.out.println("Nenhum Episodio encontrado!");
        }

//        System.out.println("A partir de que ano você deseja ver os episódios?");
//        var ano  = sc.nextInt();
//
//        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        episodios.stream()
//                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
//                .forEach(e ->
//                        System.out.println(
//                                "Temporada: " + e.getTemporada() +
//                                " Episódio: " + e.getTitulo() +
//                                " Data lançamento: " + e.getDataLancamento().format(formatador)
//                        ));



    }
}
