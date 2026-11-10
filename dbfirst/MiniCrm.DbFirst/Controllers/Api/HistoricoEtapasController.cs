using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MiniCrm.DbFirst.Data;
using MiniCrm.DbFirst.Models;

namespace MiniCrm.DbFirst.Controllers.Api
{
    [Route("api/[controller]")]
    [ApiController]
    public class HistoricoEtapasController : ControllerBase
    {
        private readonly MiniCrmContext _context;

        public HistoricoEtapasController(MiniCrmContext context)
        {
            _context = context;
        }

        // GET: api/HistoricoEtapas
        [HttpGet]
        public async Task<ActionResult<IEnumerable<HistoricoEtapa>>> GetHistoricoEtapas()
        {
            return await _context.HistoricoEtapas.ToListAsync();
        }

        // GET: api/HistoricoEtapas/5
        [HttpGet("{id}")]
        public async Task<ActionResult<HistoricoEtapa>> GetHistoricoEtapa(int id)
        {
            var historicoEtapa = await _context.HistoricoEtapas.FindAsync(id);

            if (historicoEtapa == null)
            {
                return NotFound();
            }

            return historicoEtapa;
        }

        // PUT: api/HistoricoEtapas/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutHistoricoEtapa(int id, HistoricoEtapa historicoEtapa)
        {
            if (id != historicoEtapa.IdHistorico)
            {
                return BadRequest();
            }

            _context.Entry(historicoEtapa).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!HistoricoEtapaExists(id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }

            return NoContent();
        }

        // POST: api/HistoricoEtapas
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPost]
        public async Task<ActionResult<HistoricoEtapa>> PostHistoricoEtapa(HistoricoEtapa historicoEtapa)
        {
            _context.HistoricoEtapas.Add(historicoEtapa);
            await _context.SaveChangesAsync();

            return CreatedAtAction("GetHistoricoEtapa", new { id = historicoEtapa.IdHistorico }, historicoEtapa);
        }

        // DELETE: api/HistoricoEtapas/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteHistoricoEtapa(int id)
        {
            var historicoEtapa = await _context.HistoricoEtapas.FindAsync(id);
            if (historicoEtapa == null)
            {
                return NotFound();
            }

            _context.HistoricoEtapas.Remove(historicoEtapa);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool HistoricoEtapaExists(int id)
        {
            return _context.HistoricoEtapas.Any(e => e.IdHistorico == id);
        }
    }
}
