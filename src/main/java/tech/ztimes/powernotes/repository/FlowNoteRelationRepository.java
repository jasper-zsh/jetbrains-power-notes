package tech.ztimes.powernotes.repository;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.intellij.openapi.application.ApplicationManager;
import tech.ztimes.powernotes.entity.FlowNoteRelation;
import tech.ztimes.powernotes.message.FlowNoteRelationListener;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.googlecode.cqengine.query.QueryFactory.attribute;
import static com.googlecode.cqengine.query.QueryFactory.and;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.descending;

public class FlowNoteRelationRepository {
    protected final static IndexedCollection<FlowNoteRelation> repo = new ConcurrentIndexedCollection<>();
    public static final Attribute<FlowNoteRelation, Long> FLOW_ID = attribute(FlowNoteRelation.class, Long.class, "flowId", FlowNoteRelation::getFlowId);
    public static final Attribute<FlowNoteRelation, Long> NOTE_ID = attribute(FlowNoteRelation.class, Long.class, "noteId", FlowNoteRelation::getNoteId);
    public static final Attribute<FlowNoteRelation, Integer> POSITION = attribute(FlowNoteRelation.class, Integer.class, "position", FlowNoteRelation::getPosition);

    public static FlowNoteRelationRepository getInstance() {
        return ApplicationManager.getApplication().getService(FlowNoteRelationRepository.class);
    }

    public FlowNoteRelationRepository() {
        var app = ApplicationManager.getApplication();
        app.getMessageBus().connect().subscribe(FlowNoteRelationListener.TOPIC, new FlowNoteRelationListener() {
            @Override
            public void save(FlowNoteRelation relation) {
                FlowNoteRelationRepository.this.save(relation);
            }

            @Override
            public void remove(FlowNoteRelation relation) {
                FlowNoteRelationRepository.this.remove(relation);
            }
        });
    }

    public List<FlowNoteRelation> list(Long flowId) {
        var query = equal(FLOW_ID, flowId);
        try (var relResults = repo.retrieve(query)) {
            return relResults.stream().sorted(Comparator.comparingInt(FlowNoteRelation::getPosition)).collect(Collectors.toList());
        }
    }

    public int getMaxPosition(Long flowId) {
        var query = equal(FLOW_ID, flowId);

        try (var relResults = repo.retrieve(query, queryOptions(orderBy(descending(POSITION))))) {
            var rel = relResults.stream().findFirst();
            return rel.map(relation -> relation.getPosition() + 1).orElse(1);
        }
    }

    public void save(FlowNoteRelation relation) {
        remove(relation);
        repo.add(relation);
    }

    public void remove(FlowNoteRelation relation) {
        var query = and(
                equal(FLOW_ID, relation.getFlowId()),
                equal(NOTE_ID, relation.getNoteId())
        );

        try (var resultSet = repo.retrieve(query)) {
            resultSet.stream().forEach(repo::remove);
        }
    }
}
