package org.aarteaga.test.springboot.app.services;

import org.aarteaga.test.springboot.app.models.Banco;
import org.aarteaga.test.springboot.app.models.Cuenta;
import org.aarteaga.test.springboot.app.repositories.BancoRepository;
import org.aarteaga.test.springboot.app.repositories.CuentaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CuentaServiceImpl implements  CuentaService {
    private CuentaRepository cuentaRepository;
    private BancoRepository bancoRepository;

    public CuentaServiceImpl(CuentaRepository cuentaRepository, BancoRepository bancoRepository) {
        this.cuentaRepository = cuentaRepository;
        this.bancoRepository = bancoRepository;
    }

    @Override
    public Cuenta findById(Long id) {
        return cuentaRepository.findById(id).orElseThrow();
    }

    @Override
    public int revisarTotalTransferencias(Long bancoId) {
        Banco banco = bancoRepository.findById(bancoId).orElseThrow();
        return banco.getTotalTransferencias();
    }

    @Override
    public BigDecimal revisarSaldo(Long cuentaId) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId).orElseThrow();
        return cuenta.getSaldo();
    }

    @Override
    public void transferir(Long numCuentaOrigen, Long numCuentaDestino, BigDecimal monto, Long bancoId) {
        Cuenta origen = cuentaRepository.findById(numCuentaOrigen).orElseThrow();
        origen.debito(monto);
        cuentaRepository.save(origen);

        Cuenta destino = cuentaRepository.findById(numCuentaDestino).orElseThrow();
        destino.credito(monto);
        cuentaRepository.save(destino);

        Banco banco = bancoRepository.findById(bancoId).orElseThrow();
        int totalTransferencias = banco.getTotalTransferencias();
        banco.setTotalTransferencias(++totalTransferencias);
        bancoRepository.save(banco);
    }
}
