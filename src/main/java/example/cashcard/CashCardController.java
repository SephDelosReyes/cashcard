package example.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {
    private final CashCardRepository cashCardRepository;

    public CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping("/{requestId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestId, Principal principal) {
        return ResponseEntity.of(Optional.ofNullable(findCard(requestId, principal)));
        // or below - the same thing but more verbose, or when you just wanna show off lol:
        // return cashCardRepository.findById(requestId)
        //         .map(ResponseEntity::ok)
        //         .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    private ResponseEntity<Void> createNewCard(
            @RequestBody CashCard cashCardRequest, UriComponentsBuilder ucb, Principal principal) {
        CashCard cashCardWithOwner =
                new CashCard(null, cashCardRequest.amount(), principal.getName());
        CashCard savedCashCard = cashCardRepository.save(cashCardWithOwner);
        URI locationOfNewCashCard = ucb
                .path("cashcards/{id}")
                .buildAndExpand(savedCashCard.id())
                .toUri();
        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    @GetMapping
    private ResponseEntity<Iterable<CashCard>> findAll(Pageable pageable, Principal principal) {
        Page<CashCard> page = cashCardRepository.findByOwner(
                principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                        ));
        return ResponseEntity.ok(page.getContent());
    }

    @PutMapping("/{requestId}")
    private ResponseEntity<Void> putCashCard(
            @PathVariable Long requestId, @RequestBody CashCard cashCardUpdate, Principal principal) {
        CashCard cashCard = findCard(requestId, principal);
        if (Optional.ofNullable(cashCard).isPresent()) {
            CashCard updatedCard = new CashCard(cashCard.id(), cashCardUpdate.amount(), cashCard.owner());
            cashCardRepository.save(updatedCard);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteCard(@PathVariable Long id, Principal principal) {
        if (cashCardRepository.existsByIdAndOwner(id, principal.getName())) {
            cashCardRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private CashCard findCard(Long requestId, Principal principal) {
        return cashCardRepository.findByIdAndOwner(requestId, principal.getName());
    }
}
