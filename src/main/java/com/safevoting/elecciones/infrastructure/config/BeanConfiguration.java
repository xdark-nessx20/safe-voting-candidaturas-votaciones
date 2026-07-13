package com.safevoting.elecciones.infrastructure.config;

import com.safevoting.elecciones.application.candidatura.CancelarCandidaturaUseCase;
import com.safevoting.elecciones.application.candidatura.CancelarCandidaturasPorPartidoUseCase;
import com.safevoting.elecciones.application.candidatura.InscribirCandidaturaUseCase;
import com.safevoting.elecciones.application.candidatura.ListarCandidaturasPorVotacionUseCase;
import com.safevoting.elecciones.application.miembro.ActualizarSnapshotUseCase;
import com.safevoting.elecciones.application.miembro.CrearMiembroUseCase;
import com.safevoting.elecciones.application.miembro.DarBajaMiembroUseCase;
import com.safevoting.elecciones.application.miembro.DesactivarMiembroUseCase;
import com.safevoting.elecciones.application.miembro.EditarMiembroUseCase;
import com.safevoting.elecciones.application.miembro.ListarMiembrosUseCase;
import com.safevoting.elecciones.application.miembro.ObtenerMiembroUseCase;
import com.safevoting.elecciones.application.miembro.ReactivarMiembroUseCase;
import com.safevoting.elecciones.application.partido.CrearPartidoUseCase;
import com.safevoting.elecciones.application.partido.EditarPartidoUseCase;
import com.safevoting.elecciones.application.partido.InhabilitarPartidoUseCase;
import com.safevoting.elecciones.application.partido.ListarPartidosUseCase;
import com.safevoting.elecciones.application.partido.ObtenerPartidoUseCase;
import com.safevoting.elecciones.application.votacion.AbrirVotacionUseCase;
import com.safevoting.elecciones.application.votacion.CancelarVotacionUseCase;
import com.safevoting.elecciones.application.votacion.CerrarVotacionUseCase;
import com.safevoting.elecciones.application.votacion.CompletarVotacionUseCase;
import com.safevoting.elecciones.application.votacion.CrearVotacionUseCase;
import com.safevoting.elecciones.application.votacion.EstablecerFechasUseCase;
import com.safevoting.elecciones.application.votacion.ListarVotacionesUseCase;
import com.safevoting.elecciones.application.votacion.ObtenerVotacionUseCase;
import com.safevoting.elecciones.domain.repository.CandidaturaRepository;
import com.safevoting.elecciones.domain.repository.EventLogRepository;
import com.safevoting.elecciones.domain.repository.ImageStorageService;
import com.safevoting.elecciones.domain.repository.MiembroPartidoRepository;
import com.safevoting.elecciones.domain.repository.MiembroPartidoRepository;
import com.safevoting.elecciones.domain.repository.PartidoPoliticoRepository;
import com.safevoting.elecciones.domain.repository.VotacionRepository;
import com.safevoting.elecciones.domain.repository.VotacionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public CrearPartidoUseCase crearPartidoUseCase(PartidoPoliticoRepository repository, ImageStorageService imageStorageService) {
        return new CrearPartidoUseCase(repository, imageStorageService);
    }

    @Bean
    public EditarPartidoUseCase editarPartidoUseCase(PartidoPoliticoRepository repository, ImageStorageService imageStorageService) {
        return new EditarPartidoUseCase(repository, imageStorageService);
    }

    @Bean
    public InhabilitarPartidoUseCase inhabilitarPartidoUseCase(PartidoPoliticoRepository partidoRepository,
                                                                CandidaturaRepository candidaturaRepository,
                                                                CancelarCandidaturasPorPartidoUseCase cancelarCandidaturasPorPartidoUseCase) {
        return new InhabilitarPartidoUseCase(partidoRepository, candidaturaRepository, cancelarCandidaturasPorPartidoUseCase);
    }

    @Bean
    public ListarPartidosUseCase listarPartidosUseCase(PartidoPoliticoRepository repository) {
        return new ListarPartidosUseCase(repository);
    }

    @Bean
    public ObtenerPartidoUseCase obtenerPartidoUseCase(PartidoPoliticoRepository repository) {
        return new ObtenerPartidoUseCase(repository);
    }

    @Bean
    public CrearMiembroUseCase crearMiembroUseCase(MiembroPartidoRepository miembroRepository,
                                                     PartidoPoliticoRepository partidoRepository,
                                                     ImageStorageService imageStorageService) {
        return new CrearMiembroUseCase(miembroRepository, partidoRepository, imageStorageService);
    }

    @Bean
    public ListarMiembrosUseCase listarMiembrosUseCase(MiembroPartidoRepository repository) {
        return new ListarMiembrosUseCase(repository);
    }

    @Bean
    public ObtenerMiembroUseCase obtenerMiembroUseCase(MiembroPartidoRepository repository) {
        return new ObtenerMiembroUseCase(repository);
    }

    @Bean
    public EditarMiembroUseCase editarMiembroUseCase(MiembroPartidoRepository repository, ImageStorageService imageStorageService) {
        return new EditarMiembroUseCase(repository, imageStorageService);
    }

    @Bean
    public DarBajaMiembroUseCase darBajaMiembroUseCase(MiembroPartidoRepository miembroRepository,
                                                         CandidaturaRepository candidaturaRepository) {
        return new DarBajaMiembroUseCase(miembroRepository, candidaturaRepository);
    }

    @Bean
    public ReactivarMiembroUseCase reactivarMiembroUseCase(MiembroPartidoRepository miembroRepository,
                                                             EventLogRepository eventLogRepository) {
        return new ReactivarMiembroUseCase(miembroRepository, eventLogRepository);
    }

    @Bean
    public DesactivarMiembroUseCase desactivarMiembroUseCase(MiembroPartidoRepository miembroRepository,
                                                               EventLogRepository eventLogRepository) {
        return new DesactivarMiembroUseCase(miembroRepository, eventLogRepository);
    }

    @Bean
    public ActualizarSnapshotUseCase actualizarSnapshotUseCase(MiembroPartidoRepository miembroRepository,
                                                                 CandidaturaRepository candidaturaRepository,
                                                                 EventLogRepository eventLogRepository) {
        return new ActualizarSnapshotUseCase(miembroRepository, candidaturaRepository, eventLogRepository);
    }

    @Bean
    public CrearVotacionUseCase crearVotacionUseCase(VotacionRepository repository) {
        return new CrearVotacionUseCase(repository);
    }

    @Bean
    public EstablecerFechasUseCase establecerFechasUseCase(VotacionRepository repository) {
        return new EstablecerFechasUseCase(repository);
    }

    @Bean
    public AbrirVotacionUseCase abrirVotacionUseCase(VotacionRepository repository, CandidaturaRepository candidaturaRepository) {
        return new AbrirVotacionUseCase(repository, candidaturaRepository);
    }

    @Bean
    public CerrarVotacionUseCase cerrarVotacionUseCase(VotacionRepository repository) {
        return new CerrarVotacionUseCase(repository);
    }

    @Bean
    public CancelarVotacionUseCase cancelarVotacionUseCase(VotacionRepository repository) {
        return new CancelarVotacionUseCase(repository);
    }

    @Bean
    public CompletarVotacionUseCase completarVotacionUseCase(VotacionRepository repository) {
        return new CompletarVotacionUseCase(repository);
    }

    @Bean
    public ObtenerVotacionUseCase obtenerVotacionUseCase(VotacionRepository repository) {
        return new ObtenerVotacionUseCase(repository);
    }

    @Bean
    public ListarVotacionesUseCase listarVotacionesUseCase(VotacionRepository repository) {
        return new ListarVotacionesUseCase(repository);
    }

    @Bean
    public InscribirCandidaturaUseCase inscribirCandidaturaUseCase(CandidaturaRepository candidaturaRepository,
                                                                      VotacionRepository votacionRepository,
                                                                      MiembroPartidoRepository miembroPartidoRepository,
                                                                      PartidoPoliticoRepository partidoRepository) {
        return new InscribirCandidaturaUseCase(candidaturaRepository, votacionRepository, miembroPartidoRepository, partidoRepository);
    }

    @Bean
    public CancelarCandidaturaUseCase cancelarCandidaturaUseCase(CandidaturaRepository candidaturaRepository,
                                                                   VotacionRepository votacionRepository) {
        return new CancelarCandidaturaUseCase(candidaturaRepository, votacionRepository);
    }

    @Bean
    public ListarCandidaturasPorVotacionUseCase listarCandidaturasPorVotacionUseCase(CandidaturaRepository candidaturaRepository) {
        return new ListarCandidaturasPorVotacionUseCase(candidaturaRepository);
    }

    @Bean
    public CancelarCandidaturasPorPartidoUseCase cancelarCandidaturasPorPartidoUseCase(CandidaturaRepository candidaturaRepository) {
        return new CancelarCandidaturasPorPartidoUseCase(candidaturaRepository);
    }
}
