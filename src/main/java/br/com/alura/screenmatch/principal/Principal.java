package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner sc = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=7acbe567";
    private List<DadosSerie> dadosSeries = new ArrayList<>();
    private SerieRepository serieRepository;
    private List<Serie> series;
    private Optional<Serie> serieBusca;

    public Principal(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }


    public void exibeMenu(){
        var opcao = -1;
        while(opcao != 0) {
            var menu = """
                    1  - Buscar séries
                    2  - Buscar episódios
                    3  - Listar séries buscadas
                    4  - Buscar série por titulo
                    5  - Buscar series por ator
                    6  - Top 5 séries
                    7  - Buscar séries por categoria
                    8  - Buscar séries por temporadas e avaliação
                    9  - Buscar episódio
                    10 - Top 5 episódios por série
                    11 - Buscar episódios por data
                    
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = sc.nextInt();
            sc.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriesPorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    buscarSeriesPorQtdeTemporadaEAvaliacao();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    buscarTopEpisodiosPorSerie();
                    break;
                case 11:
                    buscarEpisodiosPorData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSerieWeb() {
        serieRepository.save(new Serie(getDadosSerie()));
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = sc.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        System.out.println("Escolha uma serie pelo nome: ");
        var nomeSerie = sc.nextLine();

        Optional<Serie> serie = serieRepository
                .findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()) {
            var serieEncontrada =  serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            serieRepository.save(serieEncontrada);
        }else{
            System.out.println("Serie não encontrada!");
        }
    }

    private void listarSeriesBuscadas() {
        series = serieRepository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma serie pelo nome: ");
        var nomeSerie = sc.nextLine();
        serieBusca = serieRepository
                .findByTituloContainingIgnoreCase(nomeSerie);
        if (serieBusca.isPresent()) {
            System.out.println("Dados da serie: " + serieBusca.get());
        } else {
            System.out.println("Serie não encontrada!");
        }
    }

    private void buscarSeriesPorAtor() {
        System.out.println("Escolha uma serie pelo nome: ");
        var nomeAtor = sc.nextLine();
        System.out.println("Avaliação: ");
        var avaliacao = sc.nextDouble();

        List<Serie> seriesEncontradas = serieRepository
                .findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
        seriesEncontradas.forEach(s ->
                System.out.println(s.getTitulo() + " - Avaliação: " + s.getAvaliacao()));
    }

    private void buscarTop5Series() {
        List<Serie> topSeries = serieRepository.findTop5ByOrderByAvaliacaoDesc();
        topSeries.forEach(s ->
                System.out.println(s.getTitulo() + " - Avaliação: " + s.getAvaliacao()));
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Digite a categoria/gênero da serie: ");
        var nomeGenero = sc.nextLine();
        List<Serie> seriesPorCategoria = serieRepository
                .findByGenero(Categoria.fromPortugues(nomeGenero));
        System.out.println("Series da categoria: " + nomeGenero);
        seriesPorCategoria.forEach(System.out::println);
    }

    private void buscarSeriesPorQtdeTemporadaEAvaliacao() {
        System.out.println("Digite a quantidade de temporadas: ");
        var qtdeTemporadas = sc.nextInt();

        System.out.println("Avaliação a partir de: ");
        var avaliacao = sc.nextDouble();

        //List<Serie> seriesBuscadas = serieRepository
        //        .findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(qtdeTemporadas, avaliacao);
        List<Serie> seriesBuscadas = serieRepository.seriesPorTemporadaEAvaliacao(qtdeTemporadas, avaliacao);
        System.out.println("Series encontradas: ");
        seriesBuscadas.forEach(s ->
                System.out.println(s.getTitulo() + " - Avaliacao: " + s.getAvaliacao()));
    }

    private void buscarEpisodioPorTrecho() {
        System.out.println("Qual o nome do episódio? ");
        var nomeEpisodio = sc.nextLine();

        List<Episodio> episodiosEncontrados = serieRepository.episodiosPorTrecho(nomeEpisodio);
        System.out.println("Episódios encontrados: ");
        episodiosEncontrados.forEach(e ->
                System.out.printf("Série: %s - Temporada: %d - Episódio: %d - %s%n",
                        e.getSerie().getTitulo(), e.getTemporada(), e.getNumero(), e.getTitulo()));
    }

    private void buscarTopEpisodiosPorSerie() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            List<Episodio> topEpisodios = serieRepository.topEpisodiosPorSerie(serie);
            topEpisodios.forEach(e ->
                    System.out.printf("Série: %s - Temporada: %d - Episódio: %d - %s - Avaliação: %.1f%n",
                            e.getSerie().getTitulo(), e.getTemporada(), e.getNumero(), e.getTitulo(), e.getAvaliacao()));
        }
    }

    private void buscarEpisodiosPorData() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            System.out.println("Digite o ano mínimo de lançamento:");
            var ano = sc.nextInt();
            sc.nextLine();

            Serie serie = serieBusca.get();
            List<Episodio> episodios = serieRepository.episodiosPorSerieEAno(serie, ano);
            episodios.forEach(e ->
                    System.out.printf("Série: %s - Temporada: %d - Episódio: %d - %s - Avaliação: %.1f - Data de lançamento: %s%n",
                            e.getSerie().getTitulo(), e.getTemporada(), e.getNumero(), e.getTitulo(), e.getAvaliacao(), e.getDataLancamento()));
        }
    }
}
